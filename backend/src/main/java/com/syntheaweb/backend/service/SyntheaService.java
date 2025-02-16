package com.syntheaweb.backend.service;

import java.io.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.stereotype.Service;

/**
 * Service responsible for handling Synthea synthetic data operations,
 * including generation, compression, and deletion.
 */
@Service
public class SyntheaService {

    private static final String SYNTHEA_DIRECTORY = "/synthea/";
    private static final String SYNTHEA_OUTPUT_DIRECTORY = "/synthea/output/";
    private static final String DATAFORMAT_CSV = "csv";
    private static final String DATAFORMAT_FHIR = "fhir";


    /**
     * Generates synthetic data based on the provided parameters.
     *
     * @param populationSize The number of patients to generate.
     * @param gender         The gender of the patients ("M" or "F").
     * @param minAge         The minimum age of patients.
     * @param maxAge         The maximum age of patients.
     * @param state          The state for patient generation.
     * @param city           The city for patient generation.
     * @return The ID of the generated run.
     * @throws IOException          If there is an issue during execution.
     * @throws InterruptedException If the generation process is interrupted.
     */
    public String generateSyntheticData(Integer populationSize, String gender, Integer minAge, Integer maxAge, String state, String city) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("./run_synthea");

        addPopulationParameter(processBuilder, populationSize);
        addGenderParameter(processBuilder, gender);
        addAgeParameter(processBuilder, minAge, maxAge);
        addLocationParameter(processBuilder, state, city);

        processBuilder.directory(new File(SYNTHEA_DIRECTORY));
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        // comment this part out to hide synthea output
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("Error occurred while generating synthetic data.");
        }

        // this needs to change for multiple user support!
        return getLatestRunIDFolder();
    }

    /**
     * Retrieves the zipped folder for the generated CSV format if needed.
     *
     * @param runID The ID of the generated run.
     * @return A File representing the zipped folder.
     * @throws IOException If an error occurs during the zipping process.
     */
    public File zipCSVFolderIfNeeded(String runID) throws IOException {
        return zipFolderIfNeeded(runID, DATAFORMAT_CSV);
    }

    /**
     * Retrieves the zipped folder for the generated FHIR format if needed.
     *
     * @param runID The ID of the generated run.
     * @return A File representing the zipped folder.
     * @throws IOException If an error occurs during the zipping process.
     */
    public File zipFHIRFolderIfNeeded(String runID) throws IOException {
        return zipFolderIfNeeded(runID, DATAFORMAT_FHIR);
    }

    /**
     * Deletes all files and folders associated with a specific run ID.
     *
     * @param runId The ID of the run to delete.
     */
    public void deleteRunFiles(String runId) {
        Arrays.asList(DATAFORMAT_CSV, DATAFORMAT_FHIR).forEach(format -> {
            File runFolder = new File(SYNTHEA_OUTPUT_DIRECTORY + format + "/" + runId);
            if (runFolder.exists()) {
                deleteFolder(runFolder);
            }

            File zipFile = new File(SYNTHEA_OUTPUT_DIRECTORY + format + "/" + runId + "_" + format + ".zip");
            if (zipFile.exists()) {
                zipFile.delete();
            }
        });
    }

    // Private helper methods for internal operations

    private void addPopulationParameter(ProcessBuilder processBuilder, Integer populationSize) {
        if (populationSize != null) {
            processBuilder.command().add("-p");
            processBuilder.command().add(String.valueOf(populationSize));
        }
    }

    private void addGenderParameter(ProcessBuilder processBuilder, String gender) {
        if (gender != null && (gender.equalsIgnoreCase("M") || gender.equalsIgnoreCase("F"))) {
            processBuilder.command().add("-g");
            processBuilder.command().add(gender.toUpperCase());
        }
    }

    private void addAgeParameter(ProcessBuilder processBuilder, Integer minAge, Integer maxAge) {
        if (minAge != null && maxAge != null) {
            processBuilder.command().add("-a");
            processBuilder.command().add(minAge + "-" + maxAge);
        }
    }

    private void addLocationParameter(ProcessBuilder processBuilder, String state, String city) {
        if (state != null && !state.isEmpty()) {
            processBuilder.command().add(state);
            if (city != null && !city.isEmpty()) {
                processBuilder.command().add(city);
            }
        }
    }

    private String getLatestRunIDFolder() {
        File outputDir = new File(SYNTHEA_OUTPUT_DIRECTORY + DATAFORMAT_CSV);
        File[] directories = outputDir.listFiles(File::isDirectory);

        if (directories == null || directories.length == 0) {
            throw new RuntimeException("No output directory found in: " + outputDir.getAbsolutePath());
        }

        return Arrays.stream(directories)
                .max(Comparator.comparingLong(File::lastModified))
                .orElseThrow(() -> new RuntimeException("Could not find the latest folder."))
                .getName();
    }

    private File zipFolderIfNeeded(String runID, String format) throws IOException {
        File zipFile = new File(SYNTHEA_OUTPUT_DIRECTORY + format + "/" + runID + "_" + format + ".zip");

        if (zipFile.exists()) {
            return zipFile;
        }

        return zipFolder(runID, format);
    }

    private File zipFolder(String runID, String format) throws IOException {
        File generatedRunFolder = new File(SYNTHEA_OUTPUT_DIRECTORY + format + "/" + runID);

        if (!generatedRunFolder.exists()) {
            throw new FileNotFoundException("Folder with runID " + runID + " does not exist.");
        }

        File zippedRunFolder = new File(SYNTHEA_OUTPUT_DIRECTORY + format + "/" + runID + "_" + format + ".zip");

        try (FileOutputStream fos = new FileOutputStream(zippedRunFolder);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            for (File file : Objects.requireNonNull(generatedRunFolder.listFiles())) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    zos.putNextEntry(new ZipEntry(file.getName()));
                    fis.transferTo(zos);
                    zos.closeEntry();
                }
            }
        }

        deleteFolder(generatedRunFolder);

        return zippedRunFolder;
    }

    private void deleteFolder(File folder) {
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
            folder.delete();
        }
    }
}

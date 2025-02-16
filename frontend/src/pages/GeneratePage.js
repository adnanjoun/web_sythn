import React, { useState, useContext } from "react";
import GenerateForm from "../components/forms/GenerateForm";
import * as mui from "@mui/material";
import { saveRun } from "../services/runs/generatedRunService";
import axios from "axios";
import { AuthContext } from "../AuthContext";
import { useSnackbar } from "../components/SnackbarProvider";
import Layout from "../components/layout/Layout";

function GeneratePage() {
  const { user } = useContext(AuthContext);
  const { showSnackbar } = useSnackbar();

  const [generateOptions, setGenerateOptions] = useState({
    populationSize: "",
    gender: "all",
    minAge: "",
    maxAge: "",
    state: "",
    city: "",
  });

  const [runID, setRunID] = useState(null);
  const [loading, setLoading] = useState(false);
  const [downloading, setDownloading] = useState(false);
  const [generationTime, setGenerationTime] = useState(null);

  /**
   * handleInput
   * Updates generateOptions state for each input field.
   */
  const handleInput = (e) => {
    const { name, value } = e.target;
    setGenerateOptions((prev) => ({ ...prev, [name]: value }));
  };

  /**
   * isAgeInvalid
   * Checks these conditions:
   * 1) minAge and maxAge must both be filled if one is filled.
   * 2) minAge <= maxAge.
   * 3) Neither minAge nor maxAge can be negative.
   */
  const minAgeValue =
    generateOptions.minAge !== "" ? parseInt(generateOptions.minAge) : null;
  const maxAgeValue =
    generateOptions.maxAge !== "" ? parseInt(generateOptions.maxAge) : null;

  const isAgeEmptyMismatch =
    (minAgeValue !== null && maxAgeValue === null) ||
    (maxAgeValue !== null && minAgeValue === null);

  const isAgeRangeInvalid =
    minAgeValue !== null &&
    maxAgeValue !== null &&
    (minAgeValue > maxAgeValue || minAgeValue < 0 || maxAgeValue < 0);

  const isAgeInvalid = isAgeEmptyMismatch || isAgeRangeInvalid;

  /**
   * isPopulationSizeInvalid
   * True if populationSize is not empty AND <= 0.
   */
  const isPopulationSizeInvalid =
    generateOptions.populationSize !== "" &&
    parseInt(generateOptions.populationSize) <= 0;

  /**
   * isFormInvalid
   * check for disabling the Generate button.
   */
  const isFormInvalid = isAgeInvalid || isPopulationSizeInvalid;

  /**
   * handleGenerate
   * Makes the API call to generate synthetic data.
   */
  const handleGenerate = async (event) => {
    event.preventDefault();
    setLoading(true);

    const token = localStorage.getItem("token");
    const startTime = Date.now();

    try {
      const response = await axios.post(
        "/api/synthea/generate",
        {
          populationSize: generateOptions.populationSize || null,
          gender:
            generateOptions.gender !== "all" ? generateOptions.gender : null,
          minAge: generateOptions.minAge || null,
          maxAge: generateOptions.maxAge || null,
          state: generateOptions.state || null,
          city: generateOptions.city || null,
        },
        {
          headers: { Authorization: `Bearer ${token}` },
        }
      );

      if (!response.data.runID) {
        throw new Error("No Run ID returned from the server.");
      }

      setRunID(response.data.runID);

      const endTime = Date.now();
      const elapsedTime = ((endTime - startTime) / 1000).toFixed(2);
      setGenerationTime(elapsedTime);

      await saveRun({
        runId: response.data.runID,
        userId: user?.id,
        ...generateOptions,
      });

      showSnackbar(
        "Synthetic data generated and saved successfully!",
        "success"
      );
    } catch (error) {
      showSnackbar("Error generating or saving data.", "error");
    } finally {
      setLoading(false);
    }
  };

  /**
   * handleDownload
   * Initiates file download for the given format.
   * Displays an info snackbar on start or error snackbar if it fails.
   */
  const handleDownload = async (format) => {
    if (!runID) {
      showSnackbar("No Run available for download!", "error");
      return;
    }

    setDownloading(true);
    showSnackbar(`Download of ${format.toUpperCase()} started.`, "info");

    const token = localStorage.getItem("token");
    try {
      const response = await fetch(
        `/api/synthea/download?runID=${runID}&format=${format}`,
        {
          method: "GET",
          headers: { Authorization: `Bearer ${token}` },
        }
      );

      if (!response.ok) {
        throw new Error(`Download error: ${response.statusText}`);
      }

      const blob = await response.blob();
      const downloadUrl = window.URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = downloadUrl;
      link.download = `${runID}_${format}.zip`;
      link.click();
    } catch (error) {
      showSnackbar("Error downloading the file!", "error");
    } finally {
      setDownloading(false);
    }
  };

  /**
   * handleReturn
   * Resets to the initial state or reloads the page.
   */
  const handleReturn = () => {
    window.location.reload();
  };

  return (
    <Layout>
      <mui.Typography variant="h2" gutterBottom textAlign="center" margin={5}>
        Generate a synthetic population
      </mui.Typography>
      <GenerateForm
        generateOptions={generateOptions}
        onInputChange={handleInput}
        onSubmit={handleGenerate}
        runID={runID}
        onDownload={handleDownload}
        onReturn={handleReturn}
        loading={loading}
        downloading={downloading}
        generationTime={generationTime}
        isAgeInvalid={isAgeInvalid}
        isPopulationSizeInvalid={isPopulationSizeInvalid}
        isFormInvalid={isFormInvalid}
      />
    </Layout>
  );
}

export default GeneratePage;

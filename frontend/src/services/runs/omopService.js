import downloadService from "./downloadService";

const processRun = async (runId) => {

    const token = localStorage.getItem("token");

    const response = await fetch(
        `/api/omop/process?runId=${runId}`,
        {
            method: "POST",
            headers: {
                Authorization: `Bearer ${token}`
            }
        }
    );

    if (!response.ok) {
        throw new Error("OMOP processing failed");
    }

    return await response.text();
};

const downloadOmopExport = async (runId) => {

    const url = `/api/omop/download?runId=${runId}`;

    await downloadService.downloadWithAuth(
        url,
        `omop_${runId}.zip`
    );
};

export default {
    processRun,
    downloadOmopExport
};
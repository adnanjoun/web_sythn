import axios from "axios";

const API_URL = "/api/runs";

/**
 * getUserRuns
 * Fetches a list of runs for the authenticated user.
 *
 * @returns {Promise<Array>} - An array of runs.
 * @throws an error if the request fails.
 */
export const getUserRuns = async () => {
  try {
    const token = localStorage.getItem("token");
    const response = await axios.get(API_URL, {
      headers: { Authorization: `Bearer ${token}` },
    });
    return response.data;
  } catch (error) {
    throw error.response?.data || "Error while loading user runs.";
  }
};

/**
 * getAllRuns
 * Fetches a list of all runs (admin-only endpoint).
 *
 * @returns {Promise<Array>} - An array of runs.
 * @throws an error if the request fails.
 */
export const getAllRuns = async () => {
  try {
    const token = localStorage.getItem("token");
    const response = await axios.get(`${API_URL}/admin`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    return response.data;
  } catch (error) {
    throw error.response?.data || "Error while loading all runs.";
  }
};

/**
 * saveRun
 * Sends newly generated run data to the server to be stored in database.
 *
 * @param {Object} runData - The run information to be saved.
 * @returns {Promise<Object>} - The saved run details.
 * @throws an error if the request fails.
 */
export const saveRun = async (runData) => {
  try {
    const token = localStorage.getItem("token");
    const response = await axios.post(`${API_URL}/save`, runData, {
      headers: { Authorization: `Bearer ${token}` },
    });
    return response.data;
  } catch (error) {
    throw error.response?.data || "Error while saving the run.";
  }
};

/**
 * deleteRun
 * Deletes the specified run from the server.
 *
 * @param {string} runId - Unique identifier of the run to be deleted.
 * @throws an error if the request fails.
 */
export const deleteRun = async (runId) => {
  try {
    const token = localStorage.getItem("token");
    await axios.delete(`${API_URL}/delete/${runId}`, {
      headers: { Authorization: `Bearer ${token}` },
    });
  } catch (error) {
    throw error.response?.data || "Error while deleting the run.";
  }
};

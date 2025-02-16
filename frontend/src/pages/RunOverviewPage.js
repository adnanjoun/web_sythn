import React, { useEffect, useState, useContext } from "react";
import * as mui from "@mui/material";
import RunList from "../components/lists/RunList";
import {
  getUserRuns,
  getAllRuns,
  deleteRun,
} from "../services/runs/generatedRunService";
import { AuthContext } from "../AuthContext";
import { useSnackbar } from "../components/SnackbarProvider";
import Layout from "../components/layout/Layout"; // Snackbar hook

const RunOverviewPage = () => {
  const { user } = useContext(AuthContext);
  const { showSnackbar } = useSnackbar();
  const [runs, setRuns] = useState([]);
  const [error, setError] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [runToDelete, setRunToDelete] = useState(null);

  // Check if current user is an admin
  const isAdmin = user?.role === "ADMIN";

  /**
   * Fetch runs on component mount.
   * Admin fetches all runs, users fetch only their own runs.
   */
  useEffect(() => {
    const fetchRuns = async () => {
      try {
        const data = isAdmin ? await getAllRuns() : await getUserRuns();
        setRuns(data);
      } catch {
        setError(true);
        showSnackbar("Error loading runs.", "error");
      }
    };
    fetchRuns()
      .then(() => {
        console.log("Runs successfully fetched.");
      })
      .catch((error) => {
        console.error("Error during fetching: ", error);
      });
  }, [isAdmin, showSnackbar]);

  const openDeleteDialog = (runId) => {
    setRunToDelete(runId);
    setDeleteDialogOpen(true);
  };

  const closeDeleteDialog = () => {
    setRunToDelete(null);
    setDeleteDialogOpen(false);
  };

  /**
   * confirmDelete
   * Deletes a run after confirmation and updates the run list.
   */
  const confirmDelete = async () => {
    if (!runToDelete) return;
    try {
      await deleteRun(runToDelete);
      setRuns(runs.filter((run) => run.runId !== runToDelete));
      showSnackbar("Run successfully deleted.", "success");
    } catch {
      showSnackbar("Error deleting run.", "error");
    } finally {
      closeDeleteDialog();
    }
  };

  /**
   * handleDownload
   * Initiates file download for the given run ID and format.
   * Displays an info snackbar on start or an error snackbar if it fails.
   */
  const handleDownload = async (runId, format) => {
    const token = localStorage.getItem("token");
    if (!token) {
      showSnackbar("Error: Not authenticated!", "error");
      return;
    }

    showSnackbar(`Download of ${format.toUpperCase()} started.`, "info");

    try {
      const response = await fetch(
        `http://localhost:8080/api/synthea/download?runID=${runId}&format=${format}`,
        {
          method: "GET",
          headers: { Authorization: `Bearer ${token}` },
        }
      );

      if (!response.ok) {
        throw new Error(`Download error: ${response.statusText}`);
      }

      const blob = await response.blob();
      const downloadUrl = window.URL.createObjectURL(new Blob([blob]));
      const a = document.createElement("a");
      a.href = downloadUrl;
      a.download = `${runId}_${format}.zip`;
      a.click();
    } catch (error) {
      console.error("Download error:", error);
      showSnackbar("Error downloading the file!", "error");
    }
  };

  return (
    <Layout>
      <mui.Typography variant="h2" gutterBottom textAlign="center" margin={4}>
        Generated Runs
      </mui.Typography>

      {error ? (
        <mui.Typography color="error" textAlign="center">
          There was an error. Please try again later.
        </mui.Typography>
      ) : (
        <RunList
          runs={runs}
          isAdmin={isAdmin}
          onDelete={openDeleteDialog}
          onDownload={handleDownload}
        />
      )}

      <mui.Dialog
        open={deleteDialogOpen}
        onClose={closeDeleteDialog}
        aria-labelledby="delete-dialog-title"
        aria-describedby="delete-dialog-description"
      >
        <mui.DialogTitle id="delete-dialog-title">
          Confirm deletion
        </mui.DialogTitle>
        <mui.DialogContent>
          <mui.DialogContentText id="delete-dialog-description">
            Do you really want to delete this run? This action cannot be undone.
          </mui.DialogContentText>
        </mui.DialogContent>
        <mui.DialogActions>
          <mui.Button onClick={closeDeleteDialog}>Cancel</mui.Button>
          <mui.Button onClick={confirmDelete} color="error" variant="contained">
            Delete
          </mui.Button>
        </mui.DialogActions>
      </mui.Dialog>
    </Layout>
  );
};

export default RunOverviewPage;

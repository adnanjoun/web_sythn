import React, { useState, useEffect, useRef } from "react";
import {
  Box,
  Typography,
  Button,
  Chip,
  CircularProgress,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Alert,
  Divider,
  useTheme,
} from "@mui/material";
import {
  ExpandMore,
  AutoAwesome,
  CheckCircleOutline,
  ErrorOutline,
  HourglassEmpty,
} from "@mui/icons-material";
import ReactMarkdown from "react-markdown";
import aiService from "../../services/aiService";

const STATUS_CONFIG = {
  COMPLETED: {
    label: "Completed",
    color: "success",
    icon: <CheckCircleOutline fontSize="small" />,
  },
  PENDING: {
    label: "Generating...",
    color: "warning",
    icon: <HourglassEmpty fontSize="small" />,
  },
  FAILED: {
    label: "Failed",
    color: "error",
    icon: <ErrorOutline fontSize="small" />,
  },
};

const StatusChip = ({ status }) => {
  const cfg = STATUS_CONFIG[status] || STATUS_CONFIG.PENDING;
  return (
    <Chip
      icon={cfg.icon}
      label={cfg.label}
      color={cfg.color}
      size="small"
      sx={{
        fontSize: "0.72rem",
        fontWeight: 700,
        borderRadius: "8px",
        color: "common.white",
        "& .MuiChip-label": { px: 0.9 },
      }}
    />
  );
};

const formatDate = (isoString) => {
  if (!isoString) return "";
   return new Date(isoString).toLocaleString();
};

const AiReviewTab = ({ runId, patientId }) => {
  const theme = useTheme();
  const [summaries, setSummaries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [generating, setGenerating] = useState(false);
  const [error, setError] = useState(null);
  const [expandedId, setExpandedId] = useState(null);
  const pollRef = useRef(null);

  const fetchSummaries = async () => {
    try {
      const data = await aiService.getSummaries(patientId);
      setSummaries(data);
      return data;
    } catch (e) {
      setError("Could not load AI summaries.");
      return [];
    }
  };

  const hasPending = (data) => data.some((s) => s.status === "PENDING");

  const startPolling = (currentData) => {
    if (pollRef.current) return;
    if (!hasPending(currentData)) return;
    pollRef.current = setInterval(async () => {
      const updated = await fetchSummaries();
      if (!hasPending(updated)) {
        clearInterval(pollRef.current);
        pollRef.current = null;
      }
    }, 5000);
  };

  useEffect(() => {
    fetchSummaries().then((data) => {
      setLoading(false);
      if (data.length > 0) setExpandedId(data[0].id);
      startPolling(data);
    });
    return () => {
      if (pollRef.current) clearInterval(pollRef.current);
    };
  }, [patientId]);

  const handleGenerate = async () => {
    setGenerating(true);
    setError(null);
    try {
      await aiService.generateSummary(runId, patientId);
      const updated = await fetchSummaries();
      if (updated.length > 0) setExpandedId(updated[0].id);
      startPolling(updated);
    } catch (e) {
      setError("Failed to start generation. Please try again.");
    } finally {
      setGenerating(false);
    }
  };

  const [latest, ...older] = summaries;

  const cardSx = {
    p: 3,
    borderRadius: "12px",
    bgcolor: theme.palette.background.ternary,
    border: `1px solid ${theme.palette.divider}`,
    mb: 3,
  };

  return (
    <Box sx={{ p: 3 }}>
      <Box
        sx={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          mb: 3,
        }}
      >
        <Typography variant="h6">AI Review</Typography>
        <Button
          variant="contained"
          startIcon={
            generating ? (
              <CircularProgress size={16} color="inherit" />
            ) : (
              <AutoAwesome />
            )
          }
          onClick={handleGenerate}
          disabled={generating}
          size="small"
        >
          Generate Summary
        </Button>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {loading && (
        <Box display="flex" justifyContent="center" mt={4}>
          <CircularProgress />
        </Box>
      )}

      {!loading && summaries.length === 0 && (
        <Alert severity="info">
          No AI summaries yet. Click "Generate Summary" to create one.
        </Alert>
      )}

      {!loading && latest && (
        <Box sx={cardSx}>
          <Box
            sx={{
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
              mb: 2,
            }}
          >
            <Typography variant="subtitle1" fontWeight={700}>
              Latest Summary
            </Typography>
            <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
              <StatusChip status={latest.status} />
              <Typography variant="caption" color="text.secondary">
                {formatDate(new Date(latest.createdAt+'Z'))}
              </Typography>
            </Box>
          </Box>

          {latest.status === "PENDING" && (
            <Box
              sx={{
                display: "flex",
                alignItems: "center",
                gap: 2,
                color: "text.secondary",
              }}
            >
              <CircularProgress size={18} />
              <Typography variant="body2">
                MedGemma is generating the summary…
              </Typography>
            </Box>
          )}

          {latest.status === "COMPLETED" && (
            <ReactMarkdown>{latest.summaryText}</ReactMarkdown>
          )}

          {latest.status === "FAILED" && (
            <Alert severity="error">
              Generation failed. Try generating again.
            </Alert>
          )}
        </Box>
      )}

      {!loading && older.length > 0 && (
        <>
          <Divider sx={{ mb: 2 }} />
          <Typography variant="subtitle2" color="text.secondary" sx={{ mb: 1 }}>
            Previous Summaries
          </Typography>
          {older.map((s) => (
            <Accordion
              key={s.id}
              expanded={expandedId === s.id}
              onChange={() => setExpandedId(expandedId === s.id ? null : s.id)}
              elevation={0}
              sx={{
                bgcolor: theme.palette.background.ternary,
                border: `1px solid ${theme.palette.divider}`,
                borderRadius: "10px !important",
                mb: 1,
                "&:before": { display: "none" },
              }}
            >
              <AccordionSummary expandIcon={<ExpandMore />}>
                <Box sx={{ display: "flex", alignItems: "center", gap: 1.5 }}>
                  <StatusChip status={s.status} />
                  <Typography variant="body2" color="text.secondary">
                    {formatDate(s.createdAt)}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    • {s.modelName}
                  </Typography>
                </Box>
              </AccordionSummary>
              <AccordionDetails>
                {s.status === "COMPLETED" && (
                  <ReactMarkdown>{s.summaryText}</ReactMarkdown>
                )}
                {s.status === "FAILED" && (
                  <Alert severity="error">This generation failed.</Alert>
                )}
                {s.status === "PENDING" && (
                  <Typography variant="body2" color="text.secondary">
                    Still generating…
                  </Typography>
                )}
              </AccordionDetails>
            </Accordion>
          ))}
        </>
      )}
    </Box>
  );
};

export default AiReviewTab;

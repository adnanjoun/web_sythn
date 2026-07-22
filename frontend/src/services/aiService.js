const BASE_URL = "/api/ai";

const getHeaders = () => ({
  "Content-Type": "application/json",
  Authorization: `Bearer ${localStorage.getItem("token")}`,
});

const getSummaries = async (patientId) => {
  const res = await fetch(`${BASE_URL}/summaries?patientId=${patientId}`, {
    headers: getHeaders(),
  });
  if (!res.ok) throw new Error("Failed to fetch ai summaries");
  return res.json();
};

const generateSummary = async (runId, patientId) => {
  const res = await fetch(
    `${BASE_URL}/summaries/generate?runId=${runId}&patientId=${patientId}`,
    { method: "POST", headers: getHeaders() },
  );
  if (!res.ok) throw new Error("Failed to start ai generation");
  return res.json();
};

const aiService = { getSummaries, generateSummary };

export default aiService;

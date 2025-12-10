import { useEffect, useState } from "react";
import {
  type AgentVO,
  createAgent,
  type CreateAgentRequest,
  getAgents,
  deleteAgent,
  updateAgent,
  type UpdateAgentRequest,
} from "../api/agentApi.ts";

export function useAgents() {
  const [agents, setAgents] = useState<AgentVO[]>([]);

  useEffect(() => {
    async function fetchData() {
      const resp = await getAgents();
      setAgents(resp.agents);
    }

    fetchData().then();
  }, []);

  async function refreshAgents() {
    const resp = await getAgents();
    setAgents(resp.agents);
  }

  async function createAgentHandle(agent: CreateAgentRequest) {
    await createAgent(agent);
    await refreshAgents();
  }

  async function deleteAgentHandle(agentId: string) {
    await deleteAgent(agentId);
    await refreshAgents();
  }

  async function updateAgentHandle(
    agentId: string,
    request: UpdateAgentRequest,
  ) {
    await updateAgent(agentId, request);
    await refreshAgents();
  }

  return {
    agents,
    createAgentHandle,
    deleteAgentHandle,
    updateAgentHandle,
    refreshAgents,
  };
}

import { useEffect, useState } from "react";
import {
  type ChatSessionVO,
  getChatSessions,
  deleteChatSession,
} from "../api/agentApi.ts";

export function useChatSessions() {
  const [chatSessions, setChatSessions] = useState<ChatSessionVO[]>([]);
  const [loading, setLoading] = useState(false);

  const fetchChatSessions = async () => {
    setLoading(true);
    try {
      const resp = await getChatSessions();
      setChatSessions(resp.chatSessions);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchChatSessions();
  }, []);

  const deleteChatSessionHandle = async (chatSessionId: string) => {
    await deleteChatSession(chatSessionId);
    await fetchChatSessions();
  };

  return {
    chatSessions,
    loading,
    refreshChatSessions: fetchChatSessions,
    deleteChatSession: deleteChatSessionHandle,
  };
}


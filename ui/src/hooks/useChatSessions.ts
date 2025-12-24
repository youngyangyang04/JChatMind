import { useChatSessionsContext } from "../contexts/ChatSessionsContext.tsx";

export function useChatSessions() {
  return useChatSessionsContext();
}

import React, { useCallback, useEffect, useState } from "react";
import { useParams, useNavigate, useLocation } from "react-router-dom";
import { message as antdMessage } from "antd";
import AgentChatHistory from "./AgentChatHistory.tsx";
import AgentChatInput from "./AgentChatInput.tsx";
import {
  type ChatMessageVO,
  createChatMessage,
  createChatSession,
  getChatMessagesBySessionId,
} from "../api/agentApi.ts";
import { useAgents } from "../hooks/useAgents.ts";
import DefaultAgentChatView from "./DefaultAgentChatView.tsx";
import type { SseMessage } from "../types";

const AgentChatView: React.FC = () => {
  const { chatSessionId } = useParams<{ chatSessionId: string }>();
  const navigate = useNavigate();
  const { state } = useLocation();
  const [loading, setLoading] = useState(false);
  const { agents } = useAgents();

  // 从 URL 查询参数中获取 agentId，如果没有则使用第一个 agent
  const searchParams = new URLSearchParams(location.search);
  const agentIdFromUrl = searchParams.get("agentId");

  const defaultAgentId = agents.length > 0 ? agents[0].id : null;
  const agentId = agentIdFromUrl || defaultAgentId;

  const [messages, setMessages] = useState<ChatMessageVO[]>([]);

  const getChatMessages = useCallback(async () => {
    if (!chatSessionId) {
      return;
    }
    const resp = await getChatMessagesBySessionId(chatSessionId);
    setMessages(resp.chatMessages);
  }, [chatSessionId]);

  useEffect(() => {
    if (!chatSessionId) {
      return;
    }
    getChatMessages().then();
  }, [chatSessionId, getChatMessages]);

  const appendAssistantMessage = useCallback(
    (messageId: string, content: string) => {
      setMessages((prevMessages) => {
        const existingMessage = prevMessages.find((message) => message.id === messageId);
        if (existingMessage) {
          // 已经存在，append
          console.log("append", messageId, content);
          return prevMessages.map((message) =>
            message.id === messageId
              ? {
                  ...message,
                  content: message.content + content,
                }
              : message,
          );
        } else {
          // 不存在则增加
          console.log("add", messageId, content);
          return [
            ...prevMessages,
            {
              id: messageId,
              content: content,
              role: "assistant",
              sessionId: chatSessionId ?? "",
              metadata: {},
            },
          ];
        }
      });
    },
    [chatSessionId],
  );

  const handleSendMessage = async (value: string | { text: string }) => {
    // 处理 Sender 组件可能传递的不同格式
    const message = typeof value === "string" ? value : value.text;

    console.log(message);

    if (!message || !message.trim()) return;

    // 如果没有 chatSessionId，创建新会话
    if (!chatSessionId) {
      if (!agentId) {
        antdMessage.warning("请先创建一个智能体助手");
        return;
      }
      setLoading(true);
      try {
        const response = await createChatSession({
          agentId: agentId,
          title: message.slice(0, 20), // 使用消息的前 20 个字符作为标题
        });
        // 导航到新创建的会话
        navigate(`/chat/${response.chatSessionId}`, {
          replace: true,
          // 携带初始化消息
          state: {
            init: false,
            initMessage: message,
          },
        });
      } catch (error) {
        console.error("创建聊天会话失败:", error);
        antdMessage.error("创建聊天会话失败，请重试");
      } finally {
        setLoading(false);
      }
    } else {
      if (state?.init) {
        console.log("init", state.initMessage);
        await createChatMessage({
          agentId: agentId ?? "",
          sessionId: chatSessionId,
          role: "user",
          content: state.initMessage ?? "",
        });
      } else {
        console.log("ask", message);
        await createChatMessage({
          agentId: agentId ?? "",
          sessionId: chatSessionId,
          role: "user",
          content: message,
        });
      }
      await getChatMessages();
    }
  };

  useEffect(() => {
    // sse 连接处理, 不是对话消息不开连接
    if (!chatSessionId) {
      return;
    }
    const es = new EventSource(
      `http://localhost:8080/sse/connect/${chatSessionId}`,
    );
    es.onmessage = (event) => {
      console.log("Received message:", event.data);
    };
    es.onerror = (error) => {
      console.error("SSE error:", error);
    };

    es.addEventListener("message", (event) => {
      // 解析 JSON
      const message = JSON.parse(event.data) as SseMessage;
      if (message.type === "AI_GENERATED_CONTENT") {
        // 将 AI 生成的内容拼接进 messages
        const chatMessageId = message.metadata.chatMessageId;
        appendAssistantMessage(chatMessageId, message.payload.content);
      }
    });

    es.addEventListener("init", (event) => {
      console.log("Received init message:", event.data);
    });

    return () => {
      console.log("Closing SSE connection.");
      es.close();
    };
  }, [chatSessionId, appendAssistantMessage]);

  // 如果没有 chatSessionId，显示提示界面
  if (!chatSessionId) {
    return (
      <DefaultAgentChatView
        loading={loading}
        handleSendMessage={handleSendMessage}
      />
    );
  }

  // 如果有 chatSessionId，显示正常的聊天界面
  return (
    <div className="flex flex-col h-full">
      <div className="flex-1 px-16 pt-4 overflow-y-scroll">
        <AgentChatHistory messages={messages} />
      </div>
      <div className="border-t border-gray-200 p-4 bg-white">
        <AgentChatInput onSend={handleSendMessage} />
      </div>
    </div>
  );
};

export default AgentChatView;

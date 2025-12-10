import React, { useState } from "react";
import { MessageOutlined } from "@ant-design/icons";
import { Tabs, type TabsProps } from "antd";
import { useNavigate, useLocation } from "react-router-dom";
import AgentTabContent from "./AgentTabContent.tsx";
import AddAgentModal from "./AddAgentModal.tsx";
import ChatTabContent from "./ChatTabContent.tsx";
import KnowledgeBaseTabContent from "./KnowledgeBaseTabContent.tsx";
import AddKnowledgeBaseModal from "./AddKnowledgeBaseModal.tsx";
import { useAgents } from "../hooks/useAgents.ts";
import { useKnowledgeBases } from "../hooks/useKnowledgeBases.ts";

interface SideMenuProps {
  children?: React.ReactNode;
}

const SideMenu: React.FC<SideMenuProps> = () => {
  const navigate = useNavigate();
  const location = useLocation();

  /**
   * 添加智能体模态框状态
   */
  const [isAddAgentModalOpen, setIsAddAgentModalOpen] = useState(false);
  const toggleAddAgentModal = () => {
    setIsAddAgentModalOpen(!isAddAgentModalOpen);
    setEditingAgent(null);
  };

  /**
   * 编辑智能体状态
   */
  const [editingAgent, setEditingAgent] = useState<
    import("../api/agentApi.ts").AgentVO | null
  >(null);

  /**
   * 添加知识库模态框状态
   */
  const [isAddKnowledgeBaseModalOpen, setIsAddKnowledgeBaseModalOpen] =
    useState(false);
  const toggleAddKnowledgeBaseModal = () => {
    setIsAddKnowledgeBaseModalOpen(!isAddKnowledgeBaseModalOpen);
  };

  const {
    agents,
    createAgentHandle,
    deleteAgentHandle,
    updateAgentHandle,
  } = useAgents();
  const { knowledgeBases, createKnowledgeBaseHandle } = useKnowledgeBases();

  // 根据当前路由确定激活的标签页
  const getActiveKey = () => {
    const path = location.pathname;
    if (path.startsWith("/chat")) return "chat";
    if (path.startsWith("/knowledge-base")) return "knowledgeBase";
    return "agent"; // 默认或 /agent
  };

  // 处理标签页切换
  const handleTabChange = (key: string) => {
    switch (key) {
      case "agent":
        navigate("/agent");
        break;
      case "chat":
        navigate("/chat");
        break;
      case "knowledgeBase":
        navigate("/knowledge-base");
        break;
    }
  };

  const items: TabsProps["items"] = [
    {
      key: "agent",
      label: "智能体助手",
      children: (
        <AgentTabContent
          agents={agents}
          onSelectAgent={() => {}}
          onCreateAgentClick={toggleAddAgentModal}
          onEditAgent={(agent) => {
            setEditingAgent(agent);
            setIsAddAgentModalOpen(true);
          }}
          onDeleteAgent={deleteAgentHandle}
        />
      ),
    },
    {
      key: "chat",
      label: "聊天记录",
      children: <ChatTabContent />,
    },
    {
      key: "knowledgeBase",
      label: "知识库",
      children: (
        <KnowledgeBaseTabContent
          knowledgeBases={knowledgeBases}
          onCreateKnowledgeBaseClick={toggleAddKnowledgeBaseModal}
          onSelectKnowledgeBase={(knowledgeBaseId) => {
            navigate(`/knowledge-base/${knowledgeBaseId}`);
          }}
        />
      ),
    },
  ];

  return (
    <div className="px-4 flex flex-col h-full">
      <div className="h-14 w-full flex items-center border-b border-gray-200">
        <div className="flex items-center gap-2 mx-4">
          <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-blue-400 to-purple-400 flex items-center justify-center">
            <MessageOutlined />
          </div>
          <div className="text-lg font-semibold select-none">JChatMind</div>
        </div>
      </div>
      <div className="flex-1 min-h-0 flex flex-col">
        <Tabs
          activeKey={getActiveKey()}
          onChange={handleTabChange}
          items={items}
          className="h-full flex flex-col [&_.ant-tabs-content-holder]:flex-1 [&_.ant-tabs-content-holder]:min-h-0 [&_.ant-tabs-content]:h-full [&_.ant-tabs-tabpane]:h-full"
        />
      </div>
      <AddAgentModal
        open={isAddAgentModalOpen}
        onClose={toggleAddAgentModal}
        createAgentHandle={createAgentHandle}
        updateAgentHandle={updateAgentHandle}
        editingAgent={editingAgent}
      />
      <AddKnowledgeBaseModal
        open={isAddKnowledgeBaseModalOpen}
        onClose={toggleAddKnowledgeBaseModal}
        createKnowledgeBaseHandle={createKnowledgeBaseHandle}
      />
    </div>
  );
};

export default SideMenu;

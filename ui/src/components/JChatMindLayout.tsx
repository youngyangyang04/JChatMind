import { Routes, Route, Navigate } from "react-router-dom";
import Layout from "../layout/Layout.tsx";
import Sidebar from "../layout/Sidebar.tsx";
import SideMenu from "./SideMenu.tsx";
import Content from "../layout/Content.tsx";
import AgentChatView from "./AgentChatView.tsx";
import KnowledgeBaseView from "./KnowledgeBaseView.tsx";

export default function JChatMindLayout() {
  return (
    <Layout>
      <Sidebar>
        <SideMenu />
      </Sidebar>
      <Content>
        <Routes>
          <Route path="/" element={<Navigate to="/agent" replace />} />
          <Route path="/agent" element={<AgentChatView />} />
          <Route path="/chat" element={<AgentChatView />} />
          <Route path="/chat/:chatSessionId" element={<AgentChatView />} />
          <Route
            path="/knowledge-base"
            element={<KnowledgeBaseView />}
          />
          <Route path="/knowledge-base/:knowledgeBaseId" element={<KnowledgeBaseView />} />
        </Routes>
      </Content>
    </Layout>
  );
}

package com.serain.travelagent.chatmemory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FileBasedChatMemory 测试类
 */
class FileBasedChatMemoryTest {

    private FileBasedChatMemory chatMemory;
    private final String testDir = "./test-chat-memory";
    private final String conversationId = "test-conversation-1";

    @BeforeEach
    void setUp() {
        // 清理测试目录
        File dir = new File(testDir);
        if (dir.exists()) {
            deleteDirectory(dir);
        }
        
        // 创建新的聊天记忆实例
        chatMemory = new FileBasedChatMemory(testDir);
    }

    @Test
    void testAddAndGetMessages() {
        // 添加消息
        List<Message> messages = Arrays.asList(
            new UserMessage("你好，我想去北京旅游"),
            new AssistantMessage("北京是一个很好的旅游目的地，有很多历史文化景点")
        );
        
        chatMemory.add(conversationId, messages);
        
        // 获取消息
        List<Message> retrievedMessages = chatMemory.get(conversationId, 10);
        
        assertEquals(2, retrievedMessages.size());
        assertEquals("你好，我想去北京旅游", retrievedMessages.get(0).getText());
        assertEquals("北京是一个很好的旅游目的地，有很多历史文化景点", retrievedMessages.get(1).getText());
    }

    @Test
    void testGetLastNMessages() {
        // 添加多条消息
        for (int i = 1; i <= 5; i++) {
            chatMemory.add(conversationId, Arrays.asList(
                new UserMessage("问题 " + i),
                new AssistantMessage("回答 " + i)
            ));
        }
        
        // 获取最后3条消息
        List<Message> lastThree = chatMemory.get(conversationId, 3);
        assertEquals(3, lastThree.size());
        assertEquals("问题 4", lastThree.get(0).getText());
        assertEquals("回答 4", lastThree.get(1).getText());
        assertEquals("问题 5", lastThree.get(2).getText());
    }

    @Test
    void testClearConversation() {
        // 添加消息
        chatMemory.add(conversationId, Arrays.asList(
            new UserMessage("测试消息")
        ));
        
        // 验证消息已添加
        assertFalse(chatMemory.get(conversationId, 10).isEmpty());
        
        // 清除对话
        chatMemory.clear(conversationId);
        
        // 验证消息已被清除
        assertTrue(chatMemory.get(conversationId, 10).isEmpty());
    }

    @Test
    void testPersistenceAcrossInstances() {
        // 第一个实例添加消息
        chatMemory.add(conversationId, Arrays.asList(
            new UserMessage("持久化测试消息")
        ));
        
        // 创建新的实例（模拟应用重启）
        FileBasedChatMemory newChatMemory = new FileBasedChatMemory(testDir);
        
        // 验证新实例能读取之前保存的消息
        List<Message> retrievedMessages = newChatMemory.get(conversationId, 10);
        assertEquals(1, retrievedMessages.size());
        assertEquals("持久化测试消息", retrievedMessages.get(0).getText());
    }

    @Test
    void testMultipleConversations() {
        String conv1 = "conversation-1";
        String conv2 = "conversation-2";
        
        // 为不同对话添加消息
        chatMemory.add(conv1, Arrays.asList(new UserMessage("对话1的消息")));
        chatMemory.add(conv2, Arrays.asList(new UserMessage("对话2的消息")));
        
        // 验证各对话的消息独立存储
        List<Message> messages1 = chatMemory.get(conv1, 10);
        List<Message> messages2 = chatMemory.get(conv2, 10);
        
        assertEquals(1, messages1.size());
        assertEquals("对话1的消息", messages1.get(0).getText());
        
        assertEquals(1, messages2.size());
        assertEquals("对话2的消息", messages2.get(0).getText());
    }

    /**
     * 递归删除目录
     */
    private void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        dir.delete();
    }
}
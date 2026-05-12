package com.serain.travelagent.chatmemory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import lombok.extern.slf4j.Slf4j;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.MessageType;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @BelongsProject: travelAgent
 * @BelongsPackage: com.serain.travelagent.chatmemory
 * @Author: Serain
 * @CreateTime: 2026-05-13  19:45
 * @Description: 基于Kryo序列化的文件聊天记忆存储实现
 * @Version: 1.0
 */
@Slf4j
public class FileBasedChatMemory implements ChatMemory {
    private final String baseDirectory;
    private final Kryo kryo;
    private final Map<String, List<Message>> memoryCache = new ConcurrentHashMap<>();

    public FileBasedChatMemory(String baseDirectory) {
        this.baseDirectory = baseDirectory;
        this.kryo = createKryoInstance();
        
        // 确保基础目录存在
        File dir = new File(baseDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public FileBasedChatMemory() {
        this("./chat-memory");
    }

    private Kryo createKryoInstance() {
        Kryo kryo = new Kryo();
        // 不要求强制注册类
        kryo.setRegistrationRequired(false);
        
        // 配置Kryo以支持没有无参构造函数的类
        ((DefaultInstantiatorStrategy) kryo.getInstantiatorStrategy())
            .setFallbackInstantiatorStrategy(new StdInstantiatorStrategy());
        
        // 注册需要序列化的类
        kryo.register(ArrayList.class);
        kryo.register(Arrays.asList().getClass()); // Arrays.ArrayList
        
        // 注册Java 9+的不可变集合类
        try {
            Class<?> immutableListN = Class.forName("java.util.ImmutableCollections$ListN");
            kryo.register(immutableListN);
        } catch (ClassNotFoundException e) {
            log.debug("ImmutableCollections$ListN not available in this Java version");
        }
        
        try {
            Class<?> immutableList12 = Class.forName("java.util.ImmutableCollections$List12");
            kryo.register(immutableList12);
        } catch (ClassNotFoundException e) {
            log.debug("ImmutableCollections$List12 not available in this Java version");
        }
        
        kryo.register(UserMessage.class);
        kryo.register(AssistantMessage.class);
        kryo.register(SystemMessage.class);
        kryo.register(MessageType.class);
        kryo.register(HashMap.class);
        kryo.register(String.class);
        return kryo;
    }

    private String getConversationFilePath(String conversationId) {
        return baseDirectory + File.separator + conversationId + ".kryo";
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }

        try {
            log.info("Adding {} messages to conversation ID: {}", messages.size(), conversationId);
            
            // 从缓存或文件中加载现有消息
            List<Message> existingMessages = getFromCacheOrFile(conversationId);
            
            // 添加新消息
            existingMessages.addAll(messages);
            
            // 更新缓存
            memoryCache.put(conversationId, existingMessages);
            
            // 保存到文件
            saveToFile(conversationId, existingMessages);
            
            log.debug("Added {} messages to conversation {}. Total messages: {}", 
                messages.size(), conversationId, existingMessages.size());
        } catch (Exception e) {
            log.error("Failed to add messages to conversation {}", conversationId, e);
            throw new RuntimeException("Failed to add messages", e);
        }
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        try {
            List<Message> allMessages = getFromCacheOrFile(conversationId);
            
            // 返回最后N条消息
            if (lastN <= 0 || lastN >= allMessages.size()) {
                return new ArrayList<>(allMessages);
            }
            
            return new ArrayList<>(allMessages.subList(
                allMessages.size() - lastN, allMessages.size()));
        } catch (Exception e) {
            log.error("Failed to get messages for conversation {}", conversationId, e);
            return Collections.emptyList();
        }
    }

    @Override
    public void clear(String conversationId) {
        try {
            // 从缓存中移除
            memoryCache.remove(conversationId);
            
            // 删除文件
            String filePath = getConversationFilePath(conversationId);
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
                log.debug("Cleared conversation {} and deleted file {}", conversationId, filePath);
            }
        } catch (Exception e) {
            log.error("Failed to clear conversation {}", conversationId, e);
            throw new RuntimeException("Failed to clear conversation", e);
        }
    }

    /**
     * 从缓存或文件中获取消息列表
     */
    private List<Message> getFromCacheOrFile(String conversationId) {
        // 先从缓存中查找
        if (memoryCache.containsKey(conversationId)) {
            return new ArrayList<>(memoryCache.get(conversationId));
        }
        
        // 从文件中加载
        String filePath = getConversationFilePath(conversationId);
        File file = new File(filePath);
        
        if (file.exists()) {
            try {
                List<Message> messages = loadFromFile(filePath);
                memoryCache.put(conversationId, messages);
                return new ArrayList<>(messages);
            } catch (Exception e) {
                log.warn("Failed to load messages from file {}, deleting corrupted file and starting with empty list", filePath, e);
                // 删除损坏的文件
                if (file.delete()) {
                    log.debug("Deleted corrupted file: {}", filePath);
                }
            }
        }
        
        // 如果文件不存在或加载失败，返回空列表
        return new ArrayList<>();
    }

    /**
     * 保存消息列表到文件
     */
    private void saveToFile(String conversationId, List<Message> messages) {
        String filePath = getConversationFilePath(conversationId);
        
        // 直接写入目标文件，避免Windows文件锁定问题
        try (Output output = new Output(new FileOutputStream(filePath))) {
            kryo.writeObject(output, messages);
            output.flush();
            log.debug("Successfully saved {} messages to {}", messages.size(), filePath);
        } catch (Exception e) {
            log.error("Failed to save messages to file {}", filePath, e);
            throw new RuntimeException("Failed to save messages", e);
        }
    }

    /**
     * 从文件加载消息列表
     */
    @SuppressWarnings("unchecked")
    private List<Message> loadFromFile(String filePath) {
        File file = new File(filePath);
        
        // 检查文件是否存在且不为空
        if (!file.exists() || file.length() == 0) {
            log.warn("File {} does not exist or is empty", filePath);
            return new ArrayList<>();
        }
        
        try (Input input = new Input(new FileInputStream(filePath))) {
            // 设置足够的缓冲区大小
            input.setBuffer(new byte[8192]);
            List<Message> result = kryo.readObject(input, ArrayList.class);
            
            // 检查反序列化结果是否为null
            if (result == null) {
                log.warn("Deserialized result is null for file {}, treating as corrupted", filePath);
                file.delete();
                return new ArrayList<>();
            }
            
            log.debug("Successfully loaded {} messages from {}", result.size(), filePath);
            return result;
        } catch (Exception e) {
            log.warn("Failed to load messages from file {}, file may be corrupted. Deleting corrupted file.", filePath, e);
            // 删除损坏的文件
            file.delete();
            return new ArrayList<>();
        }
    }
}

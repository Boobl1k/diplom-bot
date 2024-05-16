package com.example.diplom_bot.client

import com.example.diplom_bot.dto.external.LLMChatRequest
import com.example.diplom_bot.dto.external.LLMChatResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(
    name = "GPT",
    url = "\${bot-properties.llm-url}",
    path = "\${bot-properties.llm-sub-path}"
)
interface LLMChatClient {
    @PostMapping("completions")
    fun postCompletions(@RequestBody llmChatRequest: LLMChatRequest): LLMChatResponse
}

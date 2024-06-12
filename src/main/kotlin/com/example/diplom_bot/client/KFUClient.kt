package com.example.diplom_bot.client

import com.example.diplom_bot.configuration.KFUClientConfiguration
import com.example.diplom_bot.dto.external.RequestTypeListDTO
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "KFU",
    url = "\${bot-properties.kfu-url}",
    path = "\${bot-properties.kfu-sub-path}",
    configuration = [KFUClientConfiguration::class]
)
interface KFUClient {
    @GetMapping("chatbot_api.get_list_request_type")
    fun getRequestTypeList(@RequestParam("p_coverage") pCoverage: Int): RequestTypeListDTO
}

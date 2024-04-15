package com.example.diplom_bot.client

import com.example.diplom_bot.dto.external.RequestTypeDTO
import org.springframework.stereotype.Component

@Component
class KfuClientAdapter(
    private val kfuClient: KFUClient
) {
    fun getRequestTypeList(): List<RequestTypeDTO> {
        val result = mutableListOf<RequestTypeDTO>()
        var pCoverage = 1
        while (true) {
            val data = kfuClient.getRequestTypeList(pCoverage).data
            if (data == null) {
                return result
            } else {
                result += data.requestTypes
                ++pCoverage
            }
        }
    }
}

package com.example.diplom_bot.dto.external

data class RequestTypeListDTO(
    val data: RequestTypesListDataDTO?
)

data class RequestTypesListDataDTO(
    val requestTypes: List<RequestTypeDTO>
)

data class RequestTypeDTO(
    val id: Int,
    val name: String
)

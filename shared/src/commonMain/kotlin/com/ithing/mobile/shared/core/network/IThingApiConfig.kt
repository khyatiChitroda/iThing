package com.ithing.mobile.shared.core.network

data class IThingApiConfig(
    val baseUrl: String = IThingApiEndpoints.BASE_URL,
    val eventsUrl: String = IThingApiEndpoints.FETCH_EVENTS_URL,
    val alarmExportUrl: String = IThingApiEndpoints.ALARM_EXPORT_URL
)

object IThingApiEndpoints {
    const val BASE_URL = "https://xqs6mwcmgfu4pfazmdzyqswfye0qxvvt.lambda-url.ap-south-1.on.aws/"
    const val FETCH_EVENTS_URL = "https://o4jvg4ubjkowz6rurqqkndzelm0tuqsq.lambda-url.ap-south-1.on.aws/fetch-events"
    const val ALARM_EXPORT_URL = "https://xqs6mwcmgfu4pfazmdzyqswfye0qxvvt.lambda-url.ap-south-1.on.aws/alarm-notification-export"
}

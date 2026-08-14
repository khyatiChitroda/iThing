package com.ithing.mobile.presentation.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class ResponsiveDesignTest {

    @Test
    fun `compact width uses one column and stacked actions`() {
        assertEquals(
            DashboardLayoutConfig(metricColumns = 1, stackActions = true),
            dashboardLayoutForWidth(419)
        )
    }

    @Test
    fun `medium lower boundary uses two columns and inline actions`() {
        assertEquals(
            DashboardLayoutConfig(metricColumns = 2, stackActions = false),
            dashboardLayoutForWidth(420)
        )
    }

    @Test
    fun `medium upper boundary uses two columns and inline actions`() {
        assertEquals(
            DashboardLayoutConfig(metricColumns = 2, stackActions = false),
            dashboardLayoutForWidth(839)
        )
    }

    @Test
    fun `expanded boundary uses three columns and inline actions`() {
        assertEquals(
            DashboardLayoutConfig(metricColumns = 3, stackActions = false),
            dashboardLayoutForWidth(840)
        )
    }
}

package com.example.habitflow

import com.example.habitflow.data.repository.GeminiRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class GeminiRepositoryTest {

    @Test
    fun testGenerateStudyPlanFallbackReturnsMicroTasks() = runBlocking {
        val repository = GeminiRepository()
        val goal = "Learn Kotlin Coroutines and Flow"
        val tasks = repository.generateStudyPlan(goal)

        assertNotNull(tasks)
        assertEquals(3, tasks.size)
        assertEquals("Research & outline key concepts for: $goal", tasks[0].title)
        assertEquals(15, tasks[0].estimatedMins)
        assertEquals("Draft core implementation / notes", tasks[1].title)
        assertEquals(25, tasks[1].estimatedMins)
        assertEquals("Review and summarize findings", tasks[2].title)
        assertEquals(15, tasks[2].estimatedMins)
    }

    @Test
    fun testStructuredMicroTaskParsing() {
        val repository = GeminiRepository()
        val rawApiResponse = """
            Here are your study tasks:
            1. Understand coroutine builders (20 mins)
            2. Explore StateFlow and SharedFlow [30 mins]
            - Practice exception handling in flows (15m)
        """.trimIndent()

        val parsedTasks = repository.parseTasksFromResponse(rawApiResponse)

        assertEquals(3, parsedTasks.size)
        assertEquals("Understand coroutine builders", parsedTasks[0].title)
        assertEquals(20, parsedTasks[0].estimatedMins)

        assertEquals("Explore StateFlow and SharedFlow", parsedTasks[1].title)
        assertEquals(30, parsedTasks[1].estimatedMins)

        assertEquals("Practice exception handling in flows", parsedTasks[2].title)
        assertEquals(15, parsedTasks[2].estimatedMins)
    }
}

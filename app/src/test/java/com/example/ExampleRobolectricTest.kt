package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.Converters
import com.example.data.model.ProblemDomain
import com.example.data.model.UrgencyLevel
import com.example.data.remote.GeminiSolutionsService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Solutions Engine", appName)
  }

  @Test
  fun `test strategy outline generation and converter serialization`() {
    val service = GeminiSolutionsService()
    val outline = service.generateFallbackStrategyOutline(
      title = "Monolith Database Deadlocks",
      description = "PostgreSQL deadlocks occurring at peak concurrency",
      domain = ProblemDomain.TECHNICAL,
      urgency = UrgencyLevel.HIGH,
      framework = "First Principles & Clean Architecture",
      constraints = "Zero downtime SLA",
      timelineWeeks = 6,
      useHighThinking = true
    )

    assertNotNull(outline.id)
    assertEquals(ProblemDomain.TECHNICAL, outline.domain)
    assertTrue(outline.strategicRoadmapPhases.isNotEmpty())
    assertTrue(outline.riskMatrix.isNotEmpty())
    assertTrue(outline.successMetrics.isNotEmpty())

    val converters = Converters()
    val entity = converters.fromStrategyOutline(outline)
    assertNotNull(entity.strategicRoadmapPhasesJson)
    assertNotNull(entity.riskMatrixJson)

    val reconstructed = converters.toStrategyOutline(entity)
    assertEquals(outline.id, reconstructed.id)
    assertEquals(outline.problemTitle, reconstructed.problemTitle)
    assertEquals(outline.strategicRoadmapPhases.size, reconstructed.strategicRoadmapPhases.size)
  }
}


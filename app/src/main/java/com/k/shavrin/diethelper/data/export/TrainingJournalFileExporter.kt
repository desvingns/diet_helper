package com.k.shavrin.diethelper.data.export

import android.content.Context
import com.k.shavrin.diethelper.domain.model.TrainingSession
import com.k.shavrin.diethelper.domain.repository.TrainingJournalExporter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject

class TrainingJournalFileExporter @Inject constructor(
    @ApplicationContext private val context: Context
) : TrainingJournalExporter {
    override suspend fun export(sessions: List<TrainingSession>): List<String> = withContext(Dispatchers.IO) {
        val directory = File(context.cacheDir, "reports").apply { mkdirs() }
        val name = "training-${UUID.randomUUID()}"
        val json = File(directory, "$name.json")
        val markdown = File(directory, "$name.md")
        json.writeText(TrainingJournalJsonCodec.encode(sessions).toString(2))
        markdown.writeText(TrainingJournalMarkdown.render(sessions))
        listOf(markdown.absolutePath, json.absolutePath)
    }
}

package com.example.daily_planner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.room.Room
import com.example.daily_planner.data.AppDatabase
import com.example.daily_planner.data.DefaultNoteRepository
import com.example.daily_planner.ui.DailyPlannerApp
import com.example.daily_planner.ui.PlannerViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "planner_database"
        ).build()

        val repository = DefaultNoteRepository(database.noteDao())
        val viewModel = PlannerViewModel(repository)

        setContent {
            DailyPlannerApp(viewModel = viewModel)
        }
    }
}
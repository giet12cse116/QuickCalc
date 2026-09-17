package com.pp.Quickcalc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.pp.Quickcalc.ui.theme.DarkBackground
import com.pp.Quickcalc.ui.theme.NeonCyan
import com.pp.Quickcalc.ui.theme.NeonPurple
import com.pp.Quickcalc.ui.theme.SurfaceCard
import com.pp.Quickcalc.ui.theme.SurfaceCardBorder
import com.pp.Quickcalc.ui.theme.TextPrimary
import com.pp.Quickcalc.ui.theme.TextSecondary
import com.pp.Quickcalc.ui.theme.WarningYellow

@Composable
fun InfoDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SurfaceCard,
            modifier = Modifier
                .fillMaxWidth()
                .height(560.dp)
                .border(1.5.dp, SurfaceCardBorder, RoundedCornerShape(24.dp))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ℹ️", fontSize = 24.sp)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "NumFlow Rules & Guide",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                }

                Spacer(Modifier.height(14.dp))

                // Scrollable Game Rules Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    RuleCard(
                        number = "1",
                        title = "Objective & Difficulty Modes",
                        description = "Choose your Difficulty Mode (Easy, Medium, Hard) on the Home Screen and tap PLAY NOW to solve equation chains!"
                    )
                    RuleCard(
                        number = "2",
                        title = "Equation Chain Solving",
                        description = "Take the Base Number and apply the Operator (+ or −) with the Delta Number. Tap the correct answer from 4 options."
                    )
                    RuleCard(
                        number = "3",
                        title = "Speed & Time Control",
                        description = "Solve each question before the timer runs out! Fast answers increase your chain score streak."
                    )
                    RuleCard(
                        number = "4",
                        title = "Lives & Hearts",
                        description = "You start with 3 Hearts. Selecting a wrong answer or letting the timer expire costs 1 Heart."
                    )
                    RuleCard(
                        number = "5",
                        title = "Progression & High Scores",
                        description = "Complete 20 questions to clear a level and advance to the next level in your chosen difficulty!"
                    )
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "GOT IT ▶",
                        fontWeight = FontWeight.Bold,
                        color = DarkBackground,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun RuleCard(
    number: String,
    title: String,
    description: String
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .border(1.dp, SurfaceCardBorder.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = CircleShape,
                color = NeonPurple.copy(alpha = 0.2f),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonPurple),
                modifier = Modifier.size(26.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = number,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonPurple
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

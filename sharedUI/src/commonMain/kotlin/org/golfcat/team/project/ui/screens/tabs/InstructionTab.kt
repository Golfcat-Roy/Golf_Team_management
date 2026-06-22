package org.golfcat.team.project.ui.screens.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme

@Composable
fun InstructionTab() {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("📖 使用說明與規則", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
        }

        item {
            InstructionCard(title = "⛳ 快速上手指南") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("1. 初次登入", fontWeight = FontWeight.Bold)
                    Text("• 點擊「使用 LINE 登入」同步資料。\n• 進入個人設定，輸入「真實姓名」與「初始差點」。\n• 輸入球隊邀請碼加入您的球隊。")
                    
                    HorizontalDivider(Modifier.padding(vertical = 4.dp))
                    
                    Text("2. 參加球賽", fontWeight = FontWeight.Bold)
                    Text("• 在「賽事列表」點擊「我要報名」。\n• 比賽時點擊「輸入成績」，系統會自動跳轉至未填洞號。\n• 使用「+ / -」計分，點擊「Next」會自動儲存成績。")
                    
                    HorizontalDivider(Modifier.padding(vertical = 4.dp))
                    
                    Text("3. 查看排行", fontWeight = FontWeight.Bold)
                    Text("• 點擊「即時排行」查看戰況，右上角「Refresh」可重新載入。")
                }
            }
        }

        item {
            InstructionCard(title = "🧮 差點計算規則") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column {
                        Text("1. 球隊差點 (Team Handicap)", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Text("結算時依據「淨桿排名」扣減差點，越強扣越多：", style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(4.dp))
                        HandicapDeductionTable()
                    }
                    
                    HorizontalDivider()

                    Column {
                        Text("2. 新新貝利亞 (New New Peoria)", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Text("由系統隨機或投票選出 6 個隱藏洞，計算公式如下：", style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "差點 = ((總桿 - 6洞隱藏洞總桿) × 1.5 - 球場標準桿) × 0.8",
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Text("淨桿 = 總桿 - 差點", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                    }
                    
                    HorizontalDivider()

                    Column {
                        Text("3. 對抗賽 (Match Play)", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Text("採「四人四球最佳球位」比洞賽，規則如下：", style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(4.dp))
                        Text("• 勝負分：比較兩組最佳成績，該洞獲勝組獲 1.0 分，平手則不給分。", style = MaterialTheme.typography.bodySmall)
                        Text("• 成就加分：不論洞勝負，個人表現可為該組額外加分：", style = MaterialTheme.typography.bodySmall)
                        Row(Modifier.padding(start = 16.dp)) {
                            Text("• HIO / 老鷹 (Eagle+)：+1.0 分\n• 小鳥 (Birdie)：+0.5 分", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        item {
            InstructionCard(title = "💡 記分卡符號說明") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    SymbolLegend("◎", "Eagle / HIO", "雙圓")
                    SymbolLegend("○", "Birdie", "單圓")
                    SymbolLegend("□", "Bogey", "單方")
                    SymbolLegend("▣", "Double Bogey+", "雙方")
                }
            }
        }

        item {
            CopyrightSection()
        }
        
        item { Spacer(Modifier.height(64.dp)) }
    }
}

@Composable
fun CopyrightSection() {
    InstructionCard(title = "📌 服務條款與聲明") {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val bodyStyle = MaterialTheme.typography.labelSmall.copy(lineHeight = androidx.compose.ui.unit.TextUnit.Unspecified)
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("• 免費版限制：人數上限 30 人，系統僅保留最近 2 場歷史賽事資料。", style = bodyStyle)
                Text("• 版權與免責：© 2026 GolfCat Team Management. 本免費版本僅供個人及非營利球隊使用，對資料儲存之完整性不負賠償責任。", style = bodyStyle)
                Text("• 隱私權聲明：GolfCat 重視您的隱私，球隊資訊、成員及成績僅供系統內部運算使用，絕對不會外洩予第三方。", style = bodyStyle)
            }

            HorizontalDivider(Modifier.padding(vertical = 4.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📧 聯絡我們：", style = bodyStyle, fontWeight = FontWeight.Bold)
                Text("golfcat.service@gmail.com", style = bodyStyle, color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

@Composable
fun InstructionCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun HandicapDeductionTable() {
    Column(Modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))) {
        Row(Modifier.background(MaterialTheme.colorScheme.primaryContainer).padding(4.dp)) {
            Text("目前差點", Modifier.weight(1.5f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text("第1名", Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
            Text("第2名", Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
            Text("第3名", Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
        }
        DeductionRow("< 10", "-1.0", "-", "-")
        DeductionRow("10 - 19.9", "-2.0", "-1.0", "-")
        DeductionRow("20 - 29.9", "-3.0", "-2.0", "-1.0")
        DeductionRow(">= 30", "-4.0", "-3.0", "-2.0")
    }
}

@Composable
fun DeductionRow(range: String, d1: String, d2: String, d3: String) {
    Row(Modifier.padding(4.dp)) {
        Text(range, Modifier.weight(1.5f), style = MaterialTheme.typography.bodySmall)
        Text(d1, Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        Text(d2, Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        Text(d3, Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
fun SymbolLegend(symbol: String, label: String, sub: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(symbol, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        Text(sub, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
    }
}

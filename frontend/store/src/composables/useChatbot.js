import { chatbotRules, chatbotFallback, chatbotGreeting, chatbotQuickQuestions } from '../data/chatbotFaq'

// 从用户输入中找到最匹配的 FAQ 回答
export function getBotReply(input, locale) {
  if (!input) return chatbotFallback[locale] || chatbotFallback['zh-CN']
  const text = String(input).toLowerCase()

  // 计算每个规则的关键词匹配数
  let best = null
  let bestCount = 0
  for (const rule of chatbotRules) {
    let count = 0
    for (const kw of rule.match) {
      // 中文关键词用小写匹配，英文关键词用子串匹配
      if (text.includes(kw.toLowerCase()) || kw.toLowerCase().includes(text)) {
        count++
      }
    }
    if (count > bestCount) {
      bestCount = count
      best = rule
    }
  }

  if (best && bestCount > 0) {
    return best.answers[locale] || best.answers['zh-CN'] || best.answers['en']
  }
  return chatbotFallback[locale] || chatbotFallback['zh-CN']
}

export function getGreeting(locale) {
  return chatbotGreeting[locale] || chatbotGreeting['zh-CN']
}

export function getQuickQuestions(locale) {
  return chatbotQuickQuestions[locale] || chatbotQuickQuestions['zh-CN']
}

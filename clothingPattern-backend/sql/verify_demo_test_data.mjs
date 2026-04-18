import fs from 'node:fs'

const file = 'clothingPattern-backend/sql/realistic_test_data_2026_0418_0501.sql'
const sql = fs.readFileSync(file, 'utf8')

if (/undefined|NaN/.test(sql)) {
  throw new Error('SQL contains undefined or NaN')
}

const forbiddenPatterns = [
  /毕业设计/,
  /本次毕业/,
  /演示/,
  /演示数据/,
  /演示管理员/,
  /demo_user/i,
  /tryon_demo/i,
  /fusion_demo/i,
  /第\d+个图案/,
  /主图案\d+/,
  /辅助面料\d+/,
]

const forbiddenPattern = forbiddenPatterns.find((pattern) => pattern.test(sql))
if (forbiddenPattern) {
  throw new Error(`SQL contains forbidden demo wording: ${forbiddenPattern}`)
}

const extractTuples = (valuesSql) => {
  const tuples = []
  let start = -1
  let depth = 0
  let inString = false
  let escaped = false

  for (let index = 0; index < valuesSql.length; index += 1) {
    const char = valuesSql[index]

    if (inString) {
      if (escaped) {
        escaped = false
      } else if (char === '\\') {
        escaped = true
      } else if (char === "'") {
        inString = false
      }
      continue
    }

    if (char === "'") {
      inString = true
      continue
    }

    if (char === '(') {
      if (depth === 0) {
        start = index
      }
      depth += 1
    } else if (char === ')') {
      depth -= 1
      if (depth === 0 && start >= 0) {
        tuples.push(valuesSql.slice(start, index + 1))
        start = -1
      }
    }
  }

  return tuples
}

const counts = {}
const insertRegExp = /INSERT INTO `([^`]+)`(?:\s*\([\s\S]*?\))?\s+VALUES\s*([\s\S]*?);/g

for (const match of sql.matchAll(insertRegExp)) {
  counts[match[1]] = extractTuples(match[2]).length
}

const badCounts = Object.entries(counts).filter(([table, count]) =>
  table === 'pattern' ? count < 1 : count < 20 || count > 30,
)
if (badCounts.length > 0) {
  throw new Error(`Row count out of range: ${JSON.stringify(badCounts)}`)
}

const sqlWithoutOriginalPattern = sql.replace(/INSERT INTO `pattern` VALUES [\s\S]*?;/g, '')
const allDates = [...sqlWithoutOriginalPattern.matchAll(/'(\d{4}-\d{2}-\d{2}) \d{2}:\d{2}:\d{2}'/g)].map(
  (match) => match[1],
)
const outOfRangeDates = allDates.filter((date) => date < '2026-04-18' || date > '2026-05-01')

if (outOfRangeDates.length > 0) {
  throw new Error(`Date out of range: ${outOfRangeDates.slice(0, 5).join(', ')}`)
}

console.log(JSON.stringify(counts, null, 2))
console.log(`dateTimeValues=${allDates.length}`)

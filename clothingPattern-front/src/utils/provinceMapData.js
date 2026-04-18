const PROVINCE_SUFFIX_REGEXP =
  /维吾尔自治区|壮族自治区|回族自治区|自治区|特别行政区|省|市/g

const normalizeProvinceAlias = (name) => {
  if (!name) {
    return ''
  }
  return String(name).trim().replace(PROVINCE_SUFFIX_REGEXP, '')
}

const normalizeCount = (count) => {
  const value = Number(count)
  return Number.isFinite(value) ? value : 0
}

const buildProvinceNameMap = (mapRegionNames = []) => {
  return mapRegionNames.reduce((nameMap, regionName) => {
    const exactName = String(regionName || '').trim()
    const aliasName = normalizeProvinceAlias(exactName)

    if (exactName) {
      nameMap.set(exactName, exactName)
    }
    if (aliasName && !nameMap.has(aliasName)) {
      nameMap.set(aliasName, exactName)
    }

    return nameMap
  }, new Map())
}

export const mapProvinceUserCountData = (data, mapRegionNames = []) => {
  if (!Array.isArray(data)) {
    return []
  }

  const provinceNameMap = buildProvinceNameMap(mapRegionNames)

  return data
    .map((item) => {
      const rawName = item?.name ?? item?.province
      const exactName = String(rawName || '').trim()
      const aliasName = normalizeProvinceAlias(exactName)
      const mapName = provinceNameMap.get(exactName) || provinceNameMap.get(aliasName) || exactName

      return {
        name: mapName,
        value: normalizeCount(item?.value ?? item?.count),
      }
    })
    .filter((item) => item.name)
}

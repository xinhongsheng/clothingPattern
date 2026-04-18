import assert from 'node:assert/strict'
import { mapProvinceUserCountData } from '../src/utils/provinceMapData.js'

const mapRegionNames = ['江西省', '广西壮族自治区', '北京市']

assert.deepEqual(
  mapProvinceUserCountData([{ province: '江西', count: '5' }], mapRegionNames),
  [{ name: '江西省', value: 5 }],
)

assert.deepEqual(
  mapProvinceUserCountData([{ province: '广西', count: '7' }], mapRegionNames),
  [{ name: '广西壮族自治区', value: 7 }],
)

assert.deepEqual(
  mapProvinceUserCountData([{ province: '北京', count: '3' }], mapRegionNames),
  [{ name: '北京市', value: 3 }],
)

console.log('provinceMapData tests passed')

/**
 * 图案相关常量和枚举
 */

// 审核状态枚举
export const AUDIT_STATUS_ENUM = {
  PENDING: 'PENDING',
  APPROVED: 'APPROVED',
  REJECTED: 'REJECTED',
} as const;

// 审核状态文本映射
export const AUDIT_STATUS_MAP: Record<string, string> = {
  PENDING: '待审核',
  APPROVED: '已通过',
  REJECTED: '已拒绝',
};

// 审核状态选项（用于下拉框）
export const AUDIT_STATUS_OPTIONS = Object.keys(AUDIT_STATUS_MAP).map((key) => {
  return {
    label: AUDIT_STATUS_MAP[key],
    value: key,
  };
});

// 生成类型枚举（新增 MJ_GENERATED）
export const GENERATION_TYPE_ENUM = {
  TEXT_GENERATED: 'TEXT_GENERATED',
  IMAGE_REFERENCED: 'IMAGE_REFERENCED',
  MJ_GENERATED: 'MJ_GENERATED', // 新增：Midjourney生成
  MANUAL_UPLOAD: '手动上传', // 新增：手动上传
} as const;

// 生成类型文本映射（新增对应中文描述）
export const GENERATION_TYPE_MAP: Record<string, string> = {
  TEXT_GENERATED: '文字生成',
  IMAGE_REFERENCED: '图片参考生成',
  MJ_GENERATED: '高清生成', // 新增：与枚举对应
  '手动上传': '手动上传', // 新增：用户上传
};

// 生成类型选项（用于下拉框）
export const GENERATION_TYPE_OPTIONS = Object.keys(GENERATION_TYPE_MAP).map((key) => {
  return {
    label: GENERATION_TYPE_MAP[key],
    value: key,
  };
});

// 审核状态颜色映射
export const AUDIT_STATUS_COLOR_MAP: Record<string, string> = {
  PENDING: 'orange',
  APPROVED: 'green',
  REJECTED: 'red',
};

// 生成类型颜色映射
export const GENERATION_TYPE_COLOR_MAP: Record<string, string> = {
  TEXT_GENERATED: 'blue',
  IMAGE_REFERENCED: 'purple',
  MJ_GENERATED: 'blue',
  '手动上传': 'green', // 新增：用户上传
};

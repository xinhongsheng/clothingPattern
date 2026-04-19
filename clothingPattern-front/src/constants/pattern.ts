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

// 生成类型枚举
export const GENERATION_TYPE_ENUM = {
  TEXT_GENERATED: 'TEXT_GENERATED',
  IMAGE_GENERATED: 'IMAGE_GENERATED',
  MANUAL_UPLOAD: '手动上传',
} as const;

// 生成类型文本映射
export const GENERATION_TYPE_MAP: Record<string, string> = {
  TEXT_GENERATED: '文字生成',
  IMAGE_GENERATED: '图片生成',
  '手动上传': '手动上传',
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
  IMAGE_GENERATED: 'purple',
  '手动上传': 'green',
};

# 模板名片 JSON 说明

本文记录模板名片 JSON v2 的字段含义、坐标换算、本地图片上传流程，以及一个实际模板的解析结果。

## 当前示例

```json
{
  "canvas": {
    "aspectRatio": 1.666667,
    "backgroundColor": "#FFFFFFFF",
    "orientation": "landscape"
  },
  "elements": [
    {
      "centerXRatio": 0.5,
      "centerYRatio": 0.5,
      "heightRatio": 0.3499659,
      "id": "a8876aea-00c2-4cb8-8a0f-8668c66adb0c",
      "image": {
        "contentScale": "fit",
        "intrinsicAspectRatio": 1.4287109,
        "sourceKind": "local_path",
        "sourceValue": "/data/data/com.zhuowei.polling/files/business_card_assets/3bcc5735-e32a-4fcb-a5c6-d0c79cf2ba22.png"
      },
      "type": "image",
      "widthRatio": 0.3,
      "zIndex": 0
    },
    {
      "centerXRatio": 0.1673373,
      "centerYRatio": 0.3707921,
      "heightRatio": 0.73475146,
      "id": "6f50a5ee-6f7f-43f1-9493-849eeb92eecc",
      "image": {
        "contentScale": "fit",
        "intrinsicAspectRatio": 0.44991213,
        "sourceKind": "local_path",
        "sourceValue": "/data/data/com.zhuowei.polling/files/business_card_assets/0cf7225b-46a6-4dc4-b493-23092cb9feff.jpg"
      },
      "type": "image",
      "widthRatio": 0.19834416,
      "zIndex": 1
    }
  ],
  "schemaVersion": 2
}
```

## 整体含义

| 字段 | 示例值 | 含义 |
|---|---|---|
| `schemaVersion` | `2` | 当前模板协议版本 |
| `canvas.orientation` | `landscape` | 横版画布 |
| `canvas.aspectRatio` | `1.666667` | 画布宽高比约为 `5:3` |
| `canvas.backgroundColor` | `#FFFFFFFF` | ARGB 格式的不透明白色 |
| `elements` | 2 个图片元素 | 按 `zIndex` 从底层到顶层排列 |

当前示例是一张白色横版名片，包含两张本地图片，没有文字元素。

## 坐标和尺寸

元素的位置和尺寸都是相对画布的归一化比例，取值通常在 `0..1`：

- `centerXRatio`：元素中心距离画布左边的比例。
- `centerYRatio`：元素中心距离画布顶部的比例。
- `widthRatio`：元素宽度占画布宽度的比例。
- `heightRatio`：元素高度占画布高度的比例。

已知画布像素尺寸为 `canvasWidth x canvasHeight` 时：

```text
centerX = centerXRatio * canvasWidth
centerY = centerYRatio * canvasHeight
width   = widthRatio   * canvasWidth
height  = heightRatio  * canvasHeight

left   = centerXRatio - widthRatio / 2
right  = centerXRatio + widthRatio / 2
top    = centerYRatio - heightRatio / 2
bottom = centerYRatio + heightRatio / 2
```

图片最终显示宽高比的校验公式：

```text
显示宽高比 = widthRatio * canvas.aspectRatio / heightRatio
```

该值应当与 `image.intrinsicAspectRatio` 基本一致。

## 元素一解析

| 属性 | 值 |
|---|---|
| ID | `a8876aea-00c2-4cb8-8a0f-8668c66adb0c` |
| 类型 | 图片 |
| 层级 | `zIndex = 0`，底层 |
| 中心位置 | `(50%, 50%)`，画布正中央 |
| 尺寸 | 画布宽度的 `30%`，画布高度的约 `35%` |
| 横向范围 | `35% .. 65%` |
| 纵向范围 | 约 `32.50% .. 67.50%` |
| 原图宽高比 | 约 `1.4287:1` |
| 显示方式 | `fit`，完整等比显示 |
| 本地文件 | `3bcc5735-e32a-4fcb-a5c6-d0c79cf2ba22.png` |

如果画布为 `1000 x 600 px`：

```text
中心点约为 (500, 300)
显示尺寸约为 300 x 210 px
```

## 元素二解析

| 属性 | 值 |
|---|---|
| ID | `6f50a5ee-6f7f-43f1-9493-849eeb92eecc` |
| 类型 | 图片 |
| 层级 | `zIndex = 1`，位于元素一上层 |
| 中心位置 | 约 `(16.73%, 37.08%)` |
| 尺寸 | 画布宽度的约 `19.83%`，画布高度的约 `73.48%` |
| 横向范围 | 约 `6.82% .. 26.65%` |
| 纵向范围 | 约 `0.34% .. 73.82%` |
| 原图宽高比 | 约 `0.4499:1`，细长竖图 |
| 显示方式 | `fit`，完整等比显示 |
| 本地文件 | `0cf7225b-46a6-4dc4-b493-23092cb9feff.jpg` |

如果画布为 `1000 x 600 px`：

```text
中心点约为 (167, 222)
显示尺寸约为 198 x 441 px
```

两个元素的横向范围没有重叠，因此本例中虽然元素二层级更高，但不会遮挡元素一。

## 图片来源

JSON v2 只支持以下两种图片来源：

### 本地图片

```json
{
  "sourceKind": "local_path",
  "sourceValue": "/data/data/com.zhuowei.polling/files/business_card_assets/example.jpg"
}
```

- 必须是应用私有目录 `files/business_card_assets` 中的真实绝对路径。
- 只能在当前应用安装环境中使用。
- 应用卸载、清除数据或换设备后，该路径会失效。
- `content://`、`file://` 和相对路径均不支持。

### 远程图片

```json
{
  "sourceKind": "remote_url",
  "sourceValue": "https://example.com/assets/example.jpg"
}
```

- 支持 `http://` 和 `https://`。
- 后台返回 HTTP(S) 地址时会按远程图片解析。
- 如果后台只替换了地址但仍保留旧的 `local_uri` 或错误的 `local_path`，合法的 HTTP(S) 地址仍会被规范化为 `remote_url`。
- 未知的 `sourceKind` 不会因为地址是 HTTP(S) 而被放行。

## 上传和替换流程

点击编辑器“完成”后：

- `EXTRA_TEMPLATE_JSON` 返回完整模板 JSON。
- `EXTRA_LOCAL_ASSET_PATHS` 返回需要上传的本地图片真实路径列表。

调用方可这样读取：

```kotlin
val templateJson = TestTemplateBusinessCardActivity.getTemplateJson(data)
val localPaths = TestTemplateBusinessCardActivity.getLocalAssetPaths(data)
```

推荐的提交后台流程：

1. 遍历 `localPaths`，使用 `File(path)` 上传图片。
2. 获取后台返回的 HTTP(S) 地址。
3. 在模板 JSON 中找到 `sourceValue == path` 的图片元素。
4. 将 `sourceKind` 改为 `remote_url`。
5. 将 `sourceValue` 替换为后台图片地址。
6. 再把替换完成的 JSON 提交后台。

替换示例：

```json
{
  "contentScale": "fit",
  "intrinsicAspectRatio": 1.4287109,
  "sourceKind": "remote_url",
  "sourceValue": "https://example.com/business-card/3bcc5735.png"
}
```

不要把 `/data/data/...` 本地路径直接作为可跨设备模板长期保存在后台。

## 层级规则

- `zIndex = 0` 表示最底层。
- 数值越大，元素越靠上。
- 多个元素重叠时，上层元素会覆盖下层元素。
- 所有 `zIndex` 必须唯一，并从 `0` 开始连续排列。

## 主要校验规则

- `schemaVersion` 当前输出为 `2`。
- 旧版只有远程图片的 v1 JSON 可以读取并规范化为 v2。
- 包含 `content://` 本地图片的旧模板会被拒绝。
- 元素 ID 必须唯一。
- `zIndex` 必须从 `0` 开始连续排列。
- 坐标和尺寸必须是有限数值。
- 每个元素必须完整位于画布范围内。
- 本地图片必须存在于应用私有素材目录中并且可以解码。
- 远程图片地址必须是带有效主机名的 HTTP(S) URL。
- 颜色统一使用大写 `#AARRGGBB` 格式。


# Text Style

Text alike components share some common style rules. The text alike components currently includes [`text`](../components/text.md) and [`input`](../components/input.md).

## Properties

- `color`: &lt;colors&gt; this property set the foreground color of an component's text content.
- `font-size`: &lt;length&gt; this property specifies the size of the font.
- `font-style`: &lt;enum&gt; `normal` | `italic`. This property lets you select italic or normal faces within a font-family. Default value is `normal`.
- `font-weight`: &lt;enum&gt; `normal` | `bold`. This property specifies the boldness of the font. Default value is `normal`.
- `text-decoration`: &lt;enum&gt; `none` | `underline` | `line-through`. This property is used to set the text formatting to underline or line-through. The default value is `none`.
- `text-align`: &lt;enum&gt; `left` | `center` | `right`. This property describes how inline content like text is aligned in its parent component. The default value is `left`.
- `font-family`:<string> this property set the font-family of the text. This property **doesn't guarteen** the given font will always be set to the text. If the specified font cannot be found at the device, a typeface fallback will occurr and the default typeface will be load. The fallback mechanism may vary in different devices.

The property `color` support multiple fomats of values, contains rgb, rgba, #fff, #ffffff, named-color.

Example:

```
.my-class { color: red; }
.my-class { color: #f00; }
.my-class { color: #ff0000; }
.my-class { color: rgb(255, 0, 0); }
.my-class { color: rgba(255, 0, 0, 0.5); }
```

## Type of Style Value

- length: number followed by length unit `px`, `px` can be omitted.
- colors: support multiple formats of values, including rgb (`rgb(255, 0, 0)`), rgba (`rgba(255, 0, 0, 0.5)`), hexadecimal (`#ff0000`), short hexadecimal (`#f00`), named color (`red`).
- enumerated values: a limited number of string values.

**Note:** [The list of color keywords.](./color-names.md)


# Changelog

## 0.0.6

_2021-08-30_

 * Don't yield a JSON decoding error on unknown fields, ignore them instead

## 0.0.5

_2021-08-22_

 * Add more circe decoder instances

## 0.0.4

_2021-08-22_

 * `SkunkPatches.updateFragment` now takes a `NonEmptyList` and no longer returns an `Option`

## 0.0.3

_2021-08-22_

 * Return a `None` from `SkunkPatches.updateFragment` when the input list is empty

## 0.0.2

_2021-08-21_

 * Add support for derived `JsonPatchCodecs`
 * Add support for derived `JsonPatchDecoders`
 * Add support for derived `JsonPatchEncoders`

## 0.0.1

_2021-08-21_

 * Initial release
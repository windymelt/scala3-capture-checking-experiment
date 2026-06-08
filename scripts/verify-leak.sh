#!/usr/bin/env bash
#
# capture checking の異常系テスト。
#
# `examples/Leak.scala` は、テナントスコープ内で取得した User をスコープの外へ
# 持ち出そうとする「漏洩コード」である。これを実ソース（src/main/scala）と一緒に
# コンパイルし、capture checking が期待どおり型エラーで拒否することを確認する。
#
# capture checking の違反はコンパイラの CheckCaptures フェーズで報告されるため、
# munit の `compileErrors`（typer フェーズまでしか走らない）では検出できない。
# そのため、実際にコンパイルを試みて失敗することをこのスクリプトで検証する。
#
# 成功（漏洩が正しく拒否された）の場合は exit 0、それ以外は exit 1 を返す。

set -u

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

EXPECTED="outlives its scope"

echo "=> 漏洩コード examples/Leak.scala のコンパイルを試みます（失敗するはず）..."
OUTPUT="$(scala-cli compile src/main/scala examples/Leak.scala \
  --scala 3.8.4 -O -language:experimental.captureChecking 2>&1)"
STATUS=$?

if [ $STATUS -eq 0 ]; then
  echo "NG: コンパイルが成功してしまいました。漏洩が検出されていません。"
  echo "$OUTPUT"
  exit 1
fi

if echo "$OUTPUT" | grep -q "$EXPECTED"; then
  echo "OK: 期待どおり capture checking がコンパイルを拒否しました。"
  echo "    検出メッセージ: \"$EXPECTED\""
  exit 0
else
  echo "NG: コンパイルは失敗しましたが、想定した capture エラーではありません。"
  echo "$OUTPUT"
  exit 1
fi

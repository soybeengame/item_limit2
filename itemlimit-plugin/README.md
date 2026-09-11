# ItemLimit プラグイン

Spigot / Paper サーバー向けに、プレイヤーの**インベントリ内における
同一アイテムの最大所持数**を制限するプラグインです。制限を超えた分は
自動的にその場（プレイヤーの足元）にドロップされます。

## 機能

- `config.yml` でアイテムごとに最大所持数を設定
- インベントリ操作（拾う・クラフト・アイテム移動など）のたびに自動チェック
- 保険として一定間隔（デフォルト1秒）でも全員をチェック
- `/itemlimit` コマンドで実行中でも設定を変更可能

## ビルド方法

Java 17 と Maven が必要です。

```bash
cd itemlimit-plugin
mvn package
```

`target/itemlimit-1.0.0.jar` が生成されるので、これをサーバーの
`plugins` フォルダに入れて再起動（またはリロード）してください。

## 設定ファイル (config.yml)

```yaml
check-interval-ticks: 20

limits:
  DIAMOND: 64
  NETHERITE_INGOT: 16
  EMERALD: 128
```

- キー：`Material` の名前（例: `DIAMOND`, `NETHERITE_INGOT`）
  - 一覧は Spigot の Material 列挙型のページを参照してください。
- 値：そのアイテムをインベントリ内に何個まで所持できるか
- ここに書かれていないアイテムは制限されません

## コマンド

`itemlimit.admin` 権限（デフォルトはOPのみ）が必要です。

| コマンド | 説明 |
|---|---|
| `/itemlimit` | 使い方を表示 |
| `/itemlimit reload` | config.yml を再読み込み |
| `/itemlimit set <アイテム名> <上限数>` | 上限を設定（即時反映・保存） |
| `/itemlimit remove <アイテム名>` | 上限を解除 |
| `/itemlimit list` | 現在の設定一覧を表示 |

例:
```
/itemlimit set DIAMOND 32
/itemlimit remove DIAMOND
```

## 動作の仕組み

1. インベントリに変化が起きるイベント（拾得・クリック・ドラッグ・
   クラフト・ログイン）を検知し、次のTickでチェックを予約します。
2. インベントリ全体を走査し、アイテムごとの合計所持数を集計します。
3. 上限を超えていた場合、スロットの後方から順にアイテムを取り除き、
   取り除いた分をプレイヤーの足元にドロップします（すぐに拾い直さ
   ないよう2秒間の拾得不可時間つき）。

## カスタマイズしたい場合

- **エンダーチェストも対象にしたい** → `InventoryLimiter#enforce` で
  `player.getEnderChest()` の中身も集計対象に加えてください。
- **ドロップではなく破棄したい** → `InventoryLimiter#dropExcess` の
  中身を削除するだけの処理に変更してください。
- **メッセージを変えたい/多言語化したい** → 各クラス内の
  `sender.sendMessage(...)` / `player.sendMessage(...)` を編集して
  ください。

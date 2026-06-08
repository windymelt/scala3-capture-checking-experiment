package dev.capslock.model

/** テナントを表すドメインモデル。
  *
  * @param id
  *   テナントを一意に識別する ID
  * @param name
  *   テナント名
  */
final case class Tenant(id: TenantId, name: String)

/** ユーザーを表すドメインモデル。
  *
  * `email` は scope に束縛された [[Email]] 値オブジェクトとして持つ。`Email` は
  * `TenantScope` を capture するため、`User` 全体も scope を capture（`User^{scope}`）
  * し、[[dev.capslock.TenantScope.withTenant]] のスコープの外へは持ち出せない。
  *
  * このドメインモデルは永続化層（`Database`）が、保存している named tuple から
  * `using TenantScope` のもとで構築する。
  *
  * @param tenantId
  *   所属するテナントの ID
  * @param name
  *   ユーザー名
  * @param email
  *   メールアドレス（scope に束縛された値オブジェクト）
  */
final case class User(tenantId: TenantId, name: String, email: Email^)

/** テナント ID。
  *
  * URL のホスト部分のサブドメイン（例: `tenant1.app.example.com` の `tenant1`）が
  * そのままテナント ID となる。`String` の単純な別名ではなく opaque type にして、
  * 任意の文字列との取り違えを型レベルで防ぐ。
  */
opaque type TenantId = String

object TenantId {
  def apply(value: String): TenantId = value

  extension (id: TenantId) def value: String = id
}

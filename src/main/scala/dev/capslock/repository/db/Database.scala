package dev.capslock.repository.db

import scala.collection.mutable

import dev.capslock.TenantScope
import dev.capslock.model.*

/** 全テナントのデータが同居する単一の物理データベースを模した最小実装。
  *
  * RDB の行を模して、テナントとユーザーを **named tuple**（テーブルの行に相当）で
  * 保持する。ドメインモデル（[[Tenant]] / [[User]] / [[Email]]）は保存形式ではなく、
  * 読み出し時にこの `Database` が組み立てる。`User` の構築では、生の文字列の email を
  * `using` で受け取った [[TenantScope]] のもとで [[Email]] に包むため、戻り値は
  * scope に束縛された `User^{scope}` となる。
  *
  * 操作メソッドはすべて `private[repository]` であり、囲みパッケージ
  * `dev.capslock.repository`（repository 層とそのサブパッケージである db 層）からのみ
  * 呼び出せる。app 層から直接 DB を操作することはできず、必ず Repository を経由する。
  */
final class Database {

  /** テナント表の行。 */
  private val tenants: mutable.ArrayBuffer[(id: TenantId, name: String)] =
    mutable.ArrayBuffer.empty

  /** ユーザー表の行。email は生の文字列として保持する。 */
  private val users: mutable.ArrayBuffer[(tenantId: TenantId, name: String, email: String)] =
    mutable.ArrayBuffer.empty

  /** テナント行を追加する。 */
  private[repository] def insertTenant(row: (id: TenantId, name: String)): Unit =
    tenants += row

  /** ユーザー行を追加する。 */
  private[repository] def insertUser(row: (tenantId: TenantId, name: String, email: String)): Unit =
    users += row

  /** ID でテナントを検索し、ドメインモデルに組み立てて返す。 */
  private[repository] def findTenant(id: TenantId): Option[Tenant] =
    tenants.find(_.id == id).map(r => Tenant(r.id, r.name))

  /** 指定テナントのユーザー行を、scope に束縛したドメインモデルに組み立てて返す。
    *
    * email の生文字列を `using` の [[TenantScope]] のもとで [[Email]] に包むため、
    * 戻り値の各 `User` は `scope` を capture する（`User^{scope}`）。
    */
  private[repository] def usersOf(tenantId: TenantId)(using scope: TenantScope^): Seq[User^{scope}] =
    users
      .filter(_.tenantId == tenantId)
      .map(r => User(r.tenantId, r.name, Email(r.email)))
      .toSeq
}

object Database {
  /** [[SeedData]] を投入済みの [[Database]] を生成する。 */
  def seeded(): Database = {
    val db = new Database
    SeedData.tenants.foreach(db.insertTenant)
    SeedData.users.foreach(db.insertUser)
    db
  }
}

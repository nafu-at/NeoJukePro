/*
 * Copyright 2020 NAFU_at.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package page.nafuchoco.neojukepro.core.module;

import page.nafuchoco.neojukepro.api.NeoJukePro;
import page.nafuchoco.neojukepro.core.command.CommandExecutor;

import java.io.File;
import java.io.InputStream;
import java.util.List;

public interface Module {

    /**
     * このモジュールがロードされる際に呼び出されます。
     * この時点ではBotの機能の殆どは使用することができませんが、
     * 今後のBotの挙動を変更することができます。
     */
    void onLoad();

    /**
     * このモジュールが有効化される際に呼び出されます。
     */
    void onEnable();

    /**
     * このモジュールが無効化される際に呼び出されます。
     */
    void onDisable();

    /**
     * @return
     */
    boolean isEnable();

    /**
     * コマンドを登録します。
     *
     * @param executor 登録するコマンド実行クラス
     */
    void registerCommand(CommandExecutor executor);

    /**
     * すべてのコマンドを登録します。
     *
     * @param executors コマンド実行クラスが格納されたList
     */
    void registerCommands(List<CommandExecutor> executors);

    /**
     * 指定したコマンド実行クラスに関連するすべてのコマンドの登録を解除します。
     *
     * @param executor 削除するコマンド実行クラス
     */
    void removeCommand(CommandExecutor executor);

    /**
     * このモジュールに紐付けられたすべてのコマンドの登録を解除します。
     */
    void removeCommands();

    /**
     * このモジュールの詳細情報を返します。
     *
     * @return モジュールの詳細情報
     */
    ModuleDescription getDescription();

    /**
     * Botのコントローラークラスを返します。
     *
     * @return Botのコントローラークラス
     */
    NeoJukePro getNeoJuke();

    /**
     * このモジュールの埋め込みリソースを取得します。
     *
     * @param filename リソースのファイル名
     * @return 見つかった場合はファイル、それ以外の場合はnull
     */
    InputStream getResources(String filename);

    /**
     * プラグインデータのファイルが格納されているフォルダを返します。
     *
     * @return ファイルが格納されているフォルダ
     */
    File getDataFolder();

    /**
     * このBotのロガーに関連付けられているモジュールロガーを返します。
     *
     * @return このモジュールに関連付けられたロガー
     */
    NeoModuleLogger getModuleLogger();

    /**
     * モジュールをロードしたクラスローダーを取得します。
     *
     * @return モジュールをロードしたクラスローダー
     */
    ClassLoader getClassLoder();
}

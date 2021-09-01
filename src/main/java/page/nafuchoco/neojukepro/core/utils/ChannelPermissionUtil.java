/*
 * Copyright 2021 NAFU_at.
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

package page.nafuchoco.neojukepro.core.utils;

import lombok.val;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChannelPermissionUtil {

    public static boolean checkAccessTextChannel(TextChannel channel, Member member) {
        return channel.canTalk(member);
    }

    public static boolean checkAccessVoiceChannel(VoiceChannel channel, Member member) {
        val defConnectPrem = member.hasPermission(Permission.VOICE_CONNECT);
        val defSpeakPerm = member.hasPermission(Permission.VOICE_SPEAK);

        List<Role> roles = member.getRoles();
        Collections.reverse(new ArrayList<>(roles));

        int roleOverrideStatus = 0;
        boolean roleOverrideConnectPerm = false;
        boolean roleOverrideSpeakPerm = false;
        for (Role role : roles) {
            val po = channel.getPermissionOverride(role);
            if (po == null)
                continue;

            if (!po.getInherit().contains(Permission.VOICE_CONNECT)) {
                roleOverrideStatus = 1;
                if (defConnectPrem)
                    roleOverrideConnectPerm = !po.getDenied().contains(Permission.VOICE_CONNECT);
                else
                    roleOverrideConnectPerm = po.getAllowed().contains(Permission.VOICE_CONNECT);
            }

            if (!po.getInherit().contains(Permission.VOICE_SPEAK)) {
                roleOverrideStatus = roleOverrideStatus + 2;
                if (defSpeakPerm)
                    roleOverrideSpeakPerm = !po.getDenied().contains(Permission.VOICE_SPEAK);
                else
                    roleOverrideSpeakPerm = po.getAllowed().contains(Permission.VOICE_SPEAK);
            }
        }

        int userOverrideStatus = 0;
        boolean userOverrideConnectPerm = false;
        boolean userOverrideSpeakPerm = false;
        val userpo = channel.getPermissionOverride(member);
        if (userpo != null) {
            if (!userpo.getInherit().contains(Permission.VOICE_CONNECT)) {
                userOverrideStatus = 1;
                if (defConnectPrem)
                    userOverrideConnectPerm = !userpo.getDenied().contains(Permission.VOICE_CONNECT);
                else
                    userOverrideConnectPerm = userpo.getAllowed().contains(Permission.VOICE_CONNECT);
            }

            if (!userpo.getInherit().contains(Permission.VOICE_SPEAK)) {
                userOverrideStatus = userOverrideStatus + 2;
                if (defSpeakPerm)
                    userOverrideSpeakPerm = !userpo.getDenied().contains(Permission.VOICE_SPEAK);
                else
                    userOverrideSpeakPerm = userpo.getAllowed().contains(Permission.VOICE_SPEAK);
            }
        }

        boolean channelConnect = defConnectPrem;
        boolean channelSpeak = defSpeakPerm;

        if (roleOverrideStatus == 1 || roleOverrideStatus == 3)
            channelConnect = roleOverrideConnectPerm;
        if (roleOverrideStatus == 2 || roleOverrideStatus == 3)
            channelSpeak = roleOverrideSpeakPerm;
        if (userOverrideStatus == 1 || userOverrideStatus == 3)
            channelConnect = userOverrideConnectPerm;
        if (userOverrideStatus == 2 || userOverrideStatus == 3)
            channelSpeak = userOverrideSpeakPerm;

        return channelConnect ? channelSpeak : false;
    }
}

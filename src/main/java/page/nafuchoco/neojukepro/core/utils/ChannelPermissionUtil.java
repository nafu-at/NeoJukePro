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
        val defViewChannel = member.hasPermission(Permission.VIEW_CHANNEL);
        val defConnectPrem = member.hasPermission(Permission.VOICE_CONNECT);
        val defSpeakPerm = member.hasPermission(Permission.VOICE_SPEAK);

        List<Role> roles = member.getRoles();
        Collections.reverse(new ArrayList<>(roles));

        int roleOverrideStatus = 0;
        boolean roleOverrideView = false;
        boolean roleOverrideConnectPerm = false;
        boolean roleOverrideSpeakPerm = false;
        for (Role role : roles) {
            val po = channel.getPermissionOverride(role);
            if (po == null)
                continue;

            if (!po.getInherit().contains(Permission.VIEW_CHANNEL)) {
                roleOverrideStatus = 1;
                if (defViewChannel)
                    roleOverrideView = !po.getDenied().contains(Permission.VIEW_CHANNEL);
                else
                    roleOverrideView = po.getAllowed().contains(Permission.VIEW_CHANNEL);
            }

            if (!po.getInherit().contains(Permission.VOICE_CONNECT)) {
                roleOverrideStatus = roleOverrideStatus + 2;
                if (defConnectPrem)
                    roleOverrideConnectPerm = !po.getDenied().contains(Permission.VOICE_CONNECT);
                else
                    roleOverrideConnectPerm = po.getAllowed().contains(Permission.VOICE_CONNECT);
            }

            if (!po.getInherit().contains(Permission.VOICE_SPEAK)) {
                roleOverrideStatus = roleOverrideStatus + 4;
                if (defSpeakPerm)
                    roleOverrideSpeakPerm = !po.getDenied().contains(Permission.VOICE_SPEAK);
                else
                    roleOverrideSpeakPerm = po.getAllowed().contains(Permission.VOICE_SPEAK);
            }
        }

        int userOverrideStatus = 0;
        boolean userOverrideView = false;
        boolean userOverrideConnectPerm = false;
        boolean userOverrideSpeakPerm = false;
        val userpo = channel.getPermissionOverride(member);
        if (userpo != null) {
            if (!userpo.getInherit().contains(Permission.VIEW_CHANNEL)) {
                userOverrideStatus = 1;
                if (defViewChannel)
                    roleOverrideView = !userpo.getDenied().contains(Permission.VIEW_CHANNEL);
                else
                    roleOverrideView = userpo.getAllowed().contains(Permission.VIEW_CHANNEL);
            }

            if (!userpo.getInherit().contains(Permission.VOICE_CONNECT)) {
                userOverrideStatus = userOverrideStatus + 2;
                if (defConnectPrem)
                    userOverrideConnectPerm = !userpo.getDenied().contains(Permission.VOICE_CONNECT);
                else
                    userOverrideConnectPerm = userpo.getAllowed().contains(Permission.VOICE_CONNECT);
            }

            if (!userpo.getInherit().contains(Permission.VOICE_SPEAK)) {
                userOverrideStatus = userOverrideStatus + 4;
                if (defSpeakPerm)
                    userOverrideSpeakPerm = !userpo.getDenied().contains(Permission.VOICE_SPEAK);
                else
                    userOverrideSpeakPerm = userpo.getAllowed().contains(Permission.VOICE_SPEAK);
            }
        }

        boolean channelView = defViewChannel;
        boolean channelConnect = defConnectPrem;
        boolean channelSpeak = defSpeakPerm;

        switch (roleOverrideStatus) {
            case 1:
                channelView = roleOverrideView;
                break;

            case 2:
                channelConnect = roleOverrideConnectPerm;
                break;

            case 3:
                channelView = roleOverrideView;
                channelConnect = roleOverrideConnectPerm;
                break;

            case 4:
                channelSpeak = roleOverrideSpeakPerm;
                break;

            case 5:
                channelView = roleOverrideView;
                channelSpeak = roleOverrideSpeakPerm;
                break;

            case 6:
                channelConnect = roleOverrideConnectPerm;
                channelSpeak = roleOverrideSpeakPerm;
                break;

            case 7:
                channelView = roleOverrideView;
                channelConnect = roleOverrideConnectPerm;
                channelSpeak = roleOverrideSpeakPerm;
                break;
        }

        switch (userOverrideStatus) {
            case 1:
                channelView = userOverrideView;
                break;

            case 2:
                channelConnect = userOverrideConnectPerm;
                break;

            case 3:
                channelView = userOverrideView;
                channelConnect = userOverrideConnectPerm;
                break;

            case 4:
                channelSpeak = userOverrideSpeakPerm;
                break;

            case 5:
                channelView = userOverrideView;
                channelSpeak = userOverrideSpeakPerm;
                break;

            case 6:
                channelConnect = userOverrideConnectPerm;
                channelSpeak = userOverrideSpeakPerm;
                break;

            case 7:
                channelView = userOverrideView;
                channelConnect = userOverrideConnectPerm;
                channelSpeak = userOverrideSpeakPerm;
                break;
        }

        return channelView && (channelConnect && channelSpeak);
    }
}

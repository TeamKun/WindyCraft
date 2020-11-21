package net.teamfruit.windycraft;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class WindyCraft extends JavaPlugin {

    private Vector vel = new Vector();
    private BukkitRunnable task;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            switch (args[0]) {
                case "vel":
                    if (args.length != 3) {
                        vel = new Vector(0, 0, 0);
                    } else {
                        try {
                            vel = new Vector(
                                    Double.parseDouble(args[1]),
                                    0,
                                    Double.parseDouble(args[2])
                            );
                        } catch (NumberFormatException e) {
                            sender.sendMessage(new ComponentBuilder()
                                    .append("[かめすたプラグイン] ").color(ChatColor.LIGHT_PURPLE)
                                    .append("引数の数字がだめ").color(ChatColor.RED)
                                    .create()
                            );
                            return true;
                        }
                    }

                    sender.sendMessage(new ComponentBuilder()
                            .append("[かめすたプラグイン] ").color(ChatColor.LIGHT_PURPLE)
                            .append("風速を " + vel + " m/s にセットした").color(ChatColor.GREEN)
                            .create()
                    );

                    return true;
                case "velrandom":
                    double radius = vel.length();
                    double angle = Math.random() * Math.PI * 2;
                    vel = new Vector(
                            radius * Math.sin(angle),
                            0,
                            radius * Math.cos(angle)
                    );

                    sender.sendMessage(new ComponentBuilder()
                            .append("[かめすたプラグイン] ").color(ChatColor.LIGHT_PURPLE)
                            .append("風速を " + vel + " m/s にセットした").color(ChatColor.GREEN)
                            .create()
                    );

                    Bukkit.broadcast(new ComponentBuilder()
                            .append("[かめすたプラグイン] ").color(ChatColor.LIGHT_PURPLE)
                            .append("風上チェンジ！").color(ChatColor.GREEN)
                            .create()
                    );
                    return true;

                case "start":
                    if (task != null) {
                        sender.sendMessage(new ComponentBuilder()
                                .append("[かめすたプラグイン] ").color(ChatColor.LIGHT_PURPLE)
                                .append("既に実行中だよ").color(ChatColor.RED)
                                .create()
                        );
                        return true;
                    }

                    task = new BukkitRunnable() {
                        @Override
                        public void run() {
                            tick();
                        }
                    };
                    task.runTaskTimer(this, 0, 1);

                    Bukkit.broadcast(new ComponentBuilder()
                            .append("[かめすたプラグイン] ").color(ChatColor.LIGHT_PURPLE)
                            .append("風速すた～～～～と！！").color(ChatColor.GREEN)
                            .create()
                    );

                    return true;
                case "stop":
                    if (task == null) {
                        sender.sendMessage(new ComponentBuilder()
                                .append("[かめすたプラグイン] ").color(ChatColor.LIGHT_PURPLE)
                                .append("実行されてない").color(ChatColor.RED)
                                .create()
                        );
                        return true;
                    }

                    task.cancel();
                    task = null;

                    Bukkit.broadcast(new ComponentBuilder()
                            .append("[かめすたプラグイン] ").color(ChatColor.LIGHT_PURPLE)
                            .append("風速ストップ").color(ChatColor.GREEN)
                            .create()
                    );

                    return true;
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        switch (args.length) {
            case 1:
                return Stream.of("vel", "start", "stop", "velrandom")
                        .filter(e -> e.startsWith(args[0]))
                        .collect(Collectors.toList());
            case 2:
            case 3:
                if ("vel".equals(args[0]))
                    return Collections.singletonList("0");
        }
        return Collections.emptyList();
    }

    private void tick() {
        Bukkit.getOnlinePlayers().forEach(p -> {
            if (p.getGameMode() != GameMode.SURVIVAL)
                return;

            Vector v = p.getVelocity();
            double velX = vel.getX() / 20 / 25;
            double velZ = vel.getZ() / 20 / 25;
            double vX = v.getX() + velX;
            double vZ = v.getZ() + velZ;
            if (Math.abs(vel.getX()) > 2)
                vX *= .9;
            if (Math.abs(vel.getZ()) > 2)
                vZ *= .9;
            p.setVelocity(new Vector(vX, v.getY(), vZ));
        });
    }

    private static double clamp(double x, double min, double max) {
        return Math.max(min, Math.min(x, max));
    }

}

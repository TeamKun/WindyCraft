package net.teamfruit.moveborder;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class MoveBorder extends JavaPlugin {

    private Vector vel;
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
                            .append("速度を " + vel + " m/s にセットした").color(ChatColor.GREEN)
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
                            .append("ボーダー移動すた～～～～と！！").color(ChatColor.GREEN)
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

                    Bukkit.broadcast(new ComponentBuilder()
                            .append("[かめすたプラグイン] ").color(ChatColor.LIGHT_PURPLE)
                            .append("ボーダー移動ストップ").color(ChatColor.GREEN)
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
                return Stream.of("vel", "start", "stop")
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
        Bukkit.getWorlds().forEach(world -> {
            if (world.getPlayers().isEmpty())
                return;

            WorldBorder worldborder = world.getWorldBorder();
            double size = worldborder.getSize() / 2 - .85;
            worldborder.setCenter(worldborder.getCenter().add(vel.clone().multiply(1d / 20d)));
            BoundingBox box = BoundingBox.of(worldborder.getCenter(), size, Float.MAX_VALUE, size);
            moveOtherPlayers(world.getPlayers().stream(), world, size, box, 2);
        });
    }

    private static void moveOtherPlayers(Stream<? extends Player> stream, World world, double size, BoundingBox box, double tpThreshold) {
        BoundingBox boxIn = BoundingBox.of(box.getCenter(), size, Float.MAX_VALUE, size);
        BoundingBox boxOut = boxIn.clone().expand(tpThreshold);
        stream
                .filter(p -> p.getWorld().equals(world))
                .forEach(p -> {
                    Location pLocation = p.getLocation();
                    Vector pVector = pLocation.toVector();
                    if (!boxIn.contains(pVector)) {
                        Vector closest = getClosestPoint(boxIn, pVector);
                        closest.setY(pLocation.getY());
                        if (!boxOut.contains(pVector)) {
                            p.teleport(pLocation.set(closest.getX(), closest.getY(), closest.getZ()));
                        } else {
                            Vector vel = closest.subtract(pVector);
                            vel.setY(p.getVelocity().getY());
                            p.setVelocity(vel);
                        }
                    }
                });
    }

    private static double clamp(double x, double min, double max) {
        return Math.max(min, Math.min(x, max));
    }

    private static Vector getClosestPoint(BoundingBox box, Vector point) {
        return new Vector(
                clamp(point.getX(), box.getMinX(), box.getMaxX()),
                clamp(point.getY(), box.getMinY(), box.getMaxY()),
                clamp(point.getZ(), box.getMinZ(), box.getMaxZ())
        );
    }

}

import hlt.*;

import java.util.ArrayList;

public class MyBot {

    public static void main(final String[] args) {
        final Networking networking = new Networking();
        final GameMap gameMap = networking.initialize("SimpleBoy");

        int me = gameMap.getMyPlayerId();

        // We now have 1 full minute to analyse the initial map.
        final String initialMapIntelligence =
                "width: " + gameMap.getWidth() +
                "; height: " + gameMap.getHeight() +
                "; players: " + gameMap.getAllPlayers().size() +
                "; planets: " + gameMap.getAllPlanets().size();
        Log.log(initialMapIntelligence);

        final ArrayList<Move> moveList = new ArrayList<>();

        int turn = 0;
        for (;;) {
            turn++;
            moveList.clear();
            networking.updateMap(gameMap);

            //ArrayList<Planet> destination = new ArrayList<>();
            ArrayList<Planet> enemy = new ArrayList<>();
            ArrayList<Planet> empty = new ArrayList<>();

            //loop through each planet and designate it
            for (final Planet planet : gameMap.getAllPlanets().values()) {
                if(planet.isOwned() && planet.getOwner() != me) {
                    enemy.add(planet);
                } else if (!planet.isFull()) {
                    empty.add(planet);
                }
            }

            for (final Ship ship : gameMap.getMyPlayer().getShips().values()) {
                boolean moved = false;
                if (ship.getDockingStatus() != Ship.DockingStatus.Undocked) {
                    continue;
                }

                Planet closestEmpty = null;
                Planet closestEnemy = null;

                for (final Planet planet : empty) {
                    if (ship.canDock(planet)) {
                        moveList.add(new DockMove(ship, planet));
                        moved = true;
                        break;
                    }

                    if (closestEmpty == null || ship.getDistanceTo(closestEmpty) > ship.getDistanceTo(planet)) {
                        closestEmpty = planet;
                    }
                }

                if(moved) {
                    continue;
                }

                if(closestEmpty != null) {
                    final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, ship, closestEmpty, Constants.MAX_SPEED);
                    if (newThrustMove != null) {
                        moveList.add(newThrustMove);
                        moved = true;
                        empty.remove(closestEmpty);
                    }
                }

                if(moved) {
                    continue;
                }

                for(final Planet planet : enemy) {
                    if (closestEnemy == null || ship.getDistanceTo(closestEnemy) > ship.getDistanceTo(planet)) {
                        closestEnemy = planet;
                    }
                }

                if(closestEnemy != null) {
                    Entity target = gameMap.getAllPlayers().get(closestEnemy.getOwner()).getShip(closestEnemy.getDockedShips().get(0));
                    final ThrustMove newThrustMove = Navigation.navigateShipIntoTarget(gameMap, ship, target, Constants.MAX_SPEED);
                    if (newThrustMove != null) {
                        moveList.add(newThrustMove);
                    }
                }

            }

            //send the moves to the client
            Networking.sendMoves(moveList);
        }
    }
}


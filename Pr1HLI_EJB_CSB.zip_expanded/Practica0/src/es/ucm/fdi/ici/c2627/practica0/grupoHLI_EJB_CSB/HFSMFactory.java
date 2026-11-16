package es.ucm.fdi.ici.c2627.practica0.grupoHLI_EJB_CSB;

import pacman.game.Constants.MOVE;

/** Construye los estados y las transiciones de forma legible: la tabla de la sección 7 del diseño, una línea por arco. */
public final class HFSMFactory {
    private HFSMFactory() {
    }

    public static HFSM build(Blackboard bb) {
        // ---- Estados (hojas)
        State harvest = new State("Harvest").doing((v, b) -> Heuristics.harvest(v, b, false));
        State reposition = new State("Reposition").doing((v, b) -> Heuristics.reposition(v, b));
        State bait = new State("Bait").doing((v, b) -> Heuristics.harvest(v, b, true));
        State evade = new State("Evade").doing(new Action() {
            @Override
            public MOVE execute(GameView in, Blackboard b) {
                return Heuristics.evade(in, b, true);
            }

            @Override
            public void onEnter(GameView in, Blackboard b) {
                b.clearCount = 0;            // la histéresis cuenta desde que se entra
            }
        });
        State hunt = new State("Hunt").doing((v, b) -> Heuristics.hunt(v, b));
        State safeHarvest = new State("SafeHarvest").doing((v, b) -> Heuristics.harvest(v, b, false));
        State retreat = new State("Retreat").doing((v, b) -> Heuristics.evade(v, b, false));

        // ---- Jerarquía
        State forage = new State("Forage").child(harvest, true).child(reposition, false)
                .child(bait, false).child(evade, false);
        State feast = new State("Feast").child(hunt, true).child(safeHarvest, false).child(retreat, false);
        State alive = new State("Alive").child(forage, true).child(feast, false);

        Action clearPanic = (v, b) -> {
            b.panic = false;
            return MOVE.NEUTRAL;
        };
        Action startPanic = (v, b) -> {
            b.panic = true;
            b.panicDeadline = b.tick + Config.PANIC_TICKS;
            return MOVE.NEUTRAL;
        };

        // ---- Transiciones del padre (se evalúan antes que las de los hijos)
        // 1. Alguien es comestible (Pac-Man se comió una píldora de poder con fantasmas fuera) -> Feast.Hunt
        forage.add(new Transition("PowerEaten", feast, (v, b) -> b.anyEdible, 1, false, null));
        // 2. Nadie es comestible (y no estamos persiguiendo una píldora de pánico) -> Forage.Harvest
        feast.add(new Transition("FearOver", forage,
                (v, b) -> !b.anyEdible && (!b.panic || b.tick > b.panicDeadline), 2, false, clearPanic));

        // ---- Transiciones de los hijos de Forage
        // 3. Fantasma normal cerca de mis próximos cruces -> Evade
        for (State s : new State[] {harvest, reposition, bait}) {
            s.add(new Transition("GhostNear", evade, (v, b) -> b.nearNow, 3, true, null));
        }
        // 4. Evade con margen amplio en 2 decisiones seguidas -> Harvest
        evade.add(new Transition("Clear", harvest, (v, b) -> b.clearCount >= 2, 4, false, null));
        // 5. Evade sin salida segura salvo por una píldora de poder -> Feast.Hunt (píldora de pánico)
        evade.add(new Transition("PanicPill", feast, (v, b) -> b.panicCandidate, 5, true, startPanic));
        // 6. Harvest sin píldoras en los pasillos adyacentes -> Reposition
        harvest.add(new Transition("NoPillsNear", reposition, (v, b) -> b.pills > 0 && !b.pillsAdjacent, 6, true, null));
        // 7. Reposition en un cruce con píldoras adyacentes -> Harvest
        reposition.add(new Transition("PillsNear", harvest, (v, b) -> b.pillsAdjacent, 7, true, null));
        // 8. Píldora de poder y fantasmas agrupados cerca -> Bait
        harvest.add(new Transition("BaitOk", bait, (v, b) -> b.baitOk, 8, true, null));
        reposition.add(new Transition("BaitOk", bait, (v, b) -> b.baitOk, 8, true, null));
        // 9. Bait sin fantasmas agrupados durante T ticks -> Harvest
        bait.add(new Transition("BaitOver", harvest, (v, b) -> b.tick - b.lastBaitOkTick >= Config.T_BAIT, 9, false, null));

        // ---- Transiciones de los hijos de Feast
        // 10. Hunt sin ningún comestible alcanzable -> SafeHarvest (salvo que vayamos a por la píldora de pánico)
        hunt.add(new Transition("NoTarget", safeHarvest, (v, b) -> !b.hasHuntTarget && !b.panic, 10, true, null));
        // 11. SafeHarvest con un objetivo alcanzable -> Hunt
        safeHarvest.add(new Transition("Target", hunt, (v, b) -> b.hasHuntTarget, 11, true, null));
        // 12. Normal cerca y poco miedo restante -> Retreat
        hunt.add(new Transition("Retreat", retreat, (v, b) -> b.nearNow && b.minEdible < Config.T_RET, 12, true, null));
        safeHarvest.add(new Transition("Retreat", retreat, (v, b) -> b.nearNow && b.minEdible < Config.T_RET, 12, true, null));
        // 13. (añadida) Retreat sin normales cerca -> SafeHarvest
        retreat.add(new Transition("Safe", safeHarvest, (v, b) -> !b.nearNow, 13, true, null));

        return new HFSM(alive, bb);
    }
}

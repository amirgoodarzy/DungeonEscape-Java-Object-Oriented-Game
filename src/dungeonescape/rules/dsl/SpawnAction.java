package dungeonescape.rules.dsl;

import dungeonescape.rules.RuleContext;

/**
 * Action that dynamically instantiates and spawns an entity into the active room.
 * Syntax: spawn("Ghost", 4, 5)
 */
public class SpawnAction implements Action {
    private static final long serialVersionUID = 1L;

    private final String entityType;
    private final Expression<?> xExpr;
    private final Expression<?> yExpr;

    public SpawnAction(String entityType, Expression<?> xExpr, Expression<?> yExpr) {
        this.entityType = entityType;
        this.xExpr = xExpr;
        this.yExpr = yExpr;
    }

    public String getEntityType() {
        return entityType;
    }

    @Override
    public void execute(RuleContext context) {
        if (context == null) return;
        Object xVal = xExpr.evaluate(context);
        Object yVal = yExpr.evaluate(context);

        int x = (xVal instanceof Number) ? ((Number) xVal).intValue() : 4;
        int y = (yVal instanceof Number) ? ((Number) yVal).intValue() : 4;

        context.spawnEntity(entityType, x, y);
    }

    @Override
    public String toString() {
        return String.format("spawn(\"%s\", %s, %s)", entityType, xExpr, yExpr);
    }
}

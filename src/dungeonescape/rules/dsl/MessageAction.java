package dungeonescape.rules.dsl;

import dungeonescape.rules.RuleContext;

/**
 * Action that broadcasts a textual announcement to the HUD message banner.
 * Syntax: message("You feel a surge of berserker rage!")
 */
public class MessageAction implements Action {
    private static final long serialVersionUID = 1L;

    private final String messageText;

    public MessageAction(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageText() {
        return messageText;
    }

    @Override
    public void execute(RuleContext context) {
        if (context != null) {
            context.addNotification(messageText);
        }
    }

    @Override
    public String toString() {
        return String.format("message(\"%s\")", messageText);
    }
}

package cz.payola.web.client.events

class SimpleEvent[A] extends UnitEvent[A, EventArgs[A]]
{
    def triggerDirectly(target: A) {
        trigger(new EventArgs[A](target))
    }
}
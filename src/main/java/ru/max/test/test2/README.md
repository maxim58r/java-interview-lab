# Описание задачи
Нужно реализовать простую систему нотификаций на сайте с методами получения последней и случайной нотификации.

# Требования к решению
- Реализуйте класс NotificationQueue, который будет представлять собой очередь из объектов Notification. Каждый Notification имеет свой текст и время создания.
- Реализуйте метод addNotification(Notification notification), который добавляет нотификацию в конец очереди
- Реализуйте метод popNotification(), который возвращает и удаляет первый элемент в очереди
- Реализуйте метод getRandomNotification(), который возвращает случайную нотификацию из очереди. Операция должна выполняться за O(1).
- Все созданные методы должны пройти юнит-тесты.

# Пример использования
```java
NotificationQueue queue = new NotificationQueue();

Notification notification1 = new Notification("Первая нотификация", new Date());
Notification notification2 = new Notification("Вторая нотификация", new Date());
Notification notification3 = new Notification("Третья нотификация", new Date());

queue.addNotification(notification1);
queue.addNotification(notification2);
queue.addNotification(notification3);

queue.popNotification(); // "Первая нотификация"
Notification randomNotification = queue.getRandomNotification();
```
package ua.epam.cargo_delivery.model.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.epam.cargo_delivery.exceptions.AppException;
import ua.epam.cargo_delivery.exceptions.DBException;
import ua.epam.cargo_delivery.model.Util;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

public class DeliveryManager {
    private static final Logger log = LogManager.getLogger(DeliveryManager.class);
    private static final DBManager db = DBManager.getInstance();

    private DeliveryManager() {
        //hide
    }

    public static List<Delivery> findDeliveries(int limit, int page) {
        try (Connection c = db.getConnection()) {
            return db.findDeliveries(c, limit, page, "id");
        } catch (SQLException | ParseException e) {
            throw new DBException("Fail get Deliveries", e);
        }
    }

    public static List<Delivery> findDeliveriesWithCargoes(int limit, int page, String orderByColumn) {
        try (Connection c = db.getConnection()) {
            return db.findDeliveriesEager(c, limit, page, orderByColumn);
        } catch (SQLException | ParseException e) {
            throw new DBException("Fail get Deliveries", e);
        }
    }

    public static void saveDelivery(Delivery delivery) {
        Connection c = null;
        try {
            c = db.getConnection();
            c.setAutoCommit(false);
            db.insertCargo(c, delivery.getCargo());
            db.insertDelivery(c, delivery);
            c.commit();
        } catch (SQLException e) {
            db.rollbackConnection(c);
            throw new DBException("Save delivery failed", e);
        } finally {
            Util.closeResource(c);
        }
    }

    public static List<Delivery> findDeliveriesForUser(int limit, int page, User user) {
        try (Connection c = db.getConnection()) {
            return db.findDeliveriesForUser(c, user, limit, page);
        } catch (SQLException | ParseException e) {
            throw new DBException("Deliveries not found", e);
        }
    }

    public static Delivery findDeliveryForUser(long idDelivery, User user) {
        try (Connection c = db.getConnection()) {
            return db.findDeliveryForUser(c, idDelivery, user.getId());
        } catch (SQLException | ParseException e) {
            throw new DBException("Delivery nor found", e);
        }
    }

    public static void updateStatusDelivery(Long id, DeliveryStatus status) {
        try (Connection c = db.getConnection()) {
            db.updateStatus(c, id, status);
        } catch (SQLException e) {
            throw new DBException("Can't update status to " + status, e);
        }
    }

    public static void updateStatusDelivery(List<Delivery> deliveries, DeliveryStatus status) {
        Connection c = null;
        try {
            c = db.getConnection();
            c.setAutoCommit(false);
            for (Delivery delivery : deliveries) {
                db.updateStatus(c, delivery.getId(), status);
            }
            c.commit();
        } catch (SQLException e) {
            db.rollbackConnection(c);
            throw new DBException("Can't update status to " + status, e);
        } finally {
            Util.closeResource(c);
        }
    }

    public static List<Delivery> findDeliveriesWithStatus(DeliveryStatus status, User user) {
        try (Connection c = db.getConnection()) {
            return db.findDeliveriesWithStatus(c, status, user);
        } catch (SQLException | ParseException e) {
            throw new DBException("Fail get deliveries for user with status = " + status, e);
        }
    }

    public static void updateDateDelivery(Long id, String deliveryDate) {
        try (Connection c = db.getConnection()) {
            db.updateDateDelivery(c, id, deliveryDate);
        } catch (SQLException e) {
            throw new DBException("Can't update deliveryDate to " + deliveryDate, e);
        }
    }

    public static Integer getNumberOfPageDeliveries(int limit) {
        try (Connection c = db.getConnection()) {
            Integer numberOfDeliveries = db.numberOfDeliveries(c);
            int numberOfPage = numberOfDeliveries / limit;
            return numberOfDeliveries % limit > 0 ? numberOfPage + 1 : numberOfPage;
        } catch (SQLException e) {
            throw new DBException("Can't find number of deliveries ", e);
        }
    }

    public static List<Delivery> findDeliveriesForReport(Long fromRegion, Long toRegion, Date createDate, Date deliveryDate) {
        try (Connection c = db.getConnection()) {
            return db.findDeliveriesReport(c, fromRegion, toRegion, createDate, deliveryDate);
        } catch (SQLException | ParseException e) {
            String message = "Can't extract xls report";
            log.error(message, e);
            throw new AppException(message, e);
        }
    }
}

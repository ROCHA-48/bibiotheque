export class Reservation {
    reservationId: number;
    bookId: number | null;
    userId: number | null;
    newBookTitle: string | null;
    status: string;
    reservationDate: Date;
    expirationDate: Date;
}

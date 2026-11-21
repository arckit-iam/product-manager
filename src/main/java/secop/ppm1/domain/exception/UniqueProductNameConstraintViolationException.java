// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.domain.exception;

public class UniqueProductNameConstraintViolationException extends RuntimeException{
    public UniqueProductNameConstraintViolationException(String message){
        super(message);
    }
}

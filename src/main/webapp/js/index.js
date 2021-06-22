function validateForm() {
    let passwordCheck = $('#passwordCheck');
    let pass = $('#registerPassword');
    if (pass.val() !== passwordCheck.val()) {
        pass.addClass('is-invalid')
        passwordCheck.addClass('is-invalid')
        alert('Passwords not same')
        return false;
    }
    if (Array.from(document.querySelectorAll('form[action=registration] .is-invalid')).length !== 0) {
        return false;
    }
    pass.addClass('is-valid')
    passwordCheck.addClass('is-valid')
    return true;
}

function checkUniqueEmail(url) {
    let email = $('#email');
    $.get(url,
        {
            check: 'email',
            param: email.val()
        },
        function (data) {
            let resp = JSON.parse(data);
            if (resp.exist) {
                email.addClass("is-invalid");
                alert("Email already exist");
                return;
            }
            email.removeClass('is-invalid');
            email.addClass('is-valid');
        })
}

function checkUniquePhone(url) {
    let phone = $('#phone');
    $.get(url,
        {
            check: 'phone',
            param: phone.val()
        },
        function (data) {
            let resp = JSON.parse(data);
            if (resp.exist) {
                phone.addClass("is-invalid");
                alert("Phone already exist");
                return;
            }
            phone.removeClass('is-invalid')
            phone.addClass("is-valid")
        })
}
window.addEventListener('load', pagination());

function pagination(colName = 'id', trigger = false, asc = true, filterObj = {fromName: '', toName: ''}) {
    let pageSize = 5;
    rootLocation = $('#homeLocation').attr('href');
    let active = document.querySelector('li.active a');
    let page = active ? active.innerText : 1;
    let url = rootLocation + 'paginationDelivery';
    $('#pagination-container').pagination({
        dataSource: url,
        locator: 'deliveries',
        triggerPagingOnInit: trigger,
        totalNumber: sessionStorage.getItem("totalNumber"),
        pageSize: pageSize,
        pageNumber: page,
        className: 'paginationjs-small d-flex justify-content-end mx-5 pb-3',
        ajax: {
            type: 'POST',
            data: {
                orderBy: colName,
                ascending: asc,
                filter: filterObj
            }
        },
        callback: function (data, pagination) {
            var html = templating(data);
            $('#data-container').html(html);
        }
    })

    function templating(data) {
        let html = '';
        $.each(data, function (index, item) {
            html += `<tr>
                <td class="table-tb-width-40">
                    <div class="d-flex flex-column">
                        <div class="flex-row flex-wrap">` + item.fromName + `</div>
                        <div class="flex-row flex-wrap text-muted fs-6">` + item.whence + `</div>
                    </div>
                </td>
                <td class="table-tb-width-40">
                    <div class="d-flex flex-column">
                        <div class="flex-row flex-wrap">` + item.toName + `</div>
                        <div class="flex-row flex-wrap text-muted fs-6">` + item.whither + `</div>
                    </div>
                </td>
                <td>` + item.distance + `</td>
                <td>` + item.price + `</td>
            </tr>`;
        });
        return html;
    }
}

initSortAndFilterTable(true);

function initSortAndFilterTable(asc) {
    let regExp = /^(fromName|toName)$/;
    let filterObj = {};
    document.querySelectorAll('th span').forEach(span => span.addEventListener('click', (() => {
        asc = !asc
        pagination(span.getAttribute('col'), true, asc, filterObj);
    })));

    document.querySelectorAll('th input').forEach(input => input.addEventListener('change', (() => {
        if (!regExp.exec(input.getAttribute('col'))) {
            return;
        }
        filterObj[input.getAttribute('col')] = input.value
        pagination(input.getAttribute('col'), true, asc, filterObj);
    })));
}

/* Tracksy Admin — runtime DOM fixes */
document.addEventListener('DOMContentLoaded', function () {
    // Remove any circle/rounded class from the brand logo so it never appears oval
    document.querySelectorAll('.brand-image').forEach(function (img) {
        img.classList.remove('img-circle', 'rounded-circle', 'rounded');
        img.style.borderRadius = '8px';
        img.style.objectFit = 'contain';
    });
});
